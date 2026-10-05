package com.playground.product.infra.redis.client

import com.playground.product.application.port.outbound.StockCachePort
import com.playground.product.application.port.outbound.StockReservationSnapshot
import com.playground.product.application.port.outbound.StockSyncSnapshot
import com.playground.product.domain.exception.StockReservationConflictException
import com.playground.product.domain.exception.StockReservationStateException
import com.playground.product.infra.redis.client.StockLuaScripts.AVAILABLE_KEY_SUFFIX
import com.playground.product.infra.redis.client.StockLuaScripts.CONFIRMED_KEY_SUFFIX
import com.playground.product.infra.redis.client.StockLuaScripts.DIRTY_SET_KEY
import com.playground.product.infra.redis.client.StockLuaScripts.RESERVED_KEY_SUFFIX
import com.playground.product.infra.redis.client.StockLuaScripts.RESERVE_STOCKS_SCRIPT
import com.playground.product.infra.redis.client.StockLuaScripts.STOCK_KEY_PREFIX
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Component

@Component
class RedisStockClient(
    private val redisTemplate: RedisTemplate<String, String>,
) : StockCachePort {
    companion object {
        private const val RESERVATION_SCAN_COUNT = 100L
    }

    private fun getAvailableKey(productId: Long): String = "$STOCK_KEY_PREFIX$productId$AVAILABLE_KEY_SUFFIX"

    private fun getReservedKey(productId: Long): String = "$STOCK_KEY_PREFIX$productId$RESERVED_KEY_SUFFIX"

    private fun getConfirmedKey(productId: Long): String = "$STOCK_KEY_PREFIX$productId$CONFIRMED_KEY_SUFFIX"

    private fun getReservationKey(orderId: Long): String = "${StockLuaScripts.RESERVATION_KEY_PREFIX}$orderId"

    private fun serializeItems(entries: List<Map.Entry<Long, Int>>): String =
        entries.joinToString(";") { (id, quantity) -> "$id:$quantity" }

    private fun deserializeItems(items: String): Map<Long, Int> =
        items.split(';').associate { item ->
            val (id, quantity) = item.split(':')
            id.toLong() to quantity.toInt()
        }

    private fun failedProductId(orderId: Long, result: Long, entries: List<Map.Entry<Long, Int>>): Long? =
        when (result) {
            StockLuaScripts.SUCCESS -> null
            StockLuaScripts.ITEMS_CONFLICT -> throw StockReservationConflictException(orderId)
            StockLuaScripts.INVALID_STATE -> throw StockReservationStateException(orderId)
            else -> entries[(result - 1).toInt()].key
        }

    override fun reserveStocks(
        orderId: Long,
        quantitiesByProductId: Map<Long, Int>,
    ): Long? = reserveStocksForTransaction(orderId, quantitiesByProductId, "")

    override fun reserveStocksForTransaction(orderId: Long, quantities: Map<Long, Int>, attemptId: String): Long? {
        if (quantities.isEmpty()) {
            return null
        }

        val entries = quantities.toSortedMap().entries.toList()
        val keys =
            listOf(getReservationKey(orderId)) + entries.flatMap { (productId, _) ->
                listOf(getAvailableKey(productId), getReservedKey(productId), getConfirmedKey(productId))
            } + StockLuaScripts.PENDING_RESERVATIONS_KEY
        val items = serializeItems(entries)
        val args = entries.map { it.value.toString() }.toTypedArray()
        val failedIndex = redisTemplate.execute(RESERVE_STOCKS_SCRIPT, keys, items, attemptId, *args)
        return failedProductId(orderId, failedIndex, entries)
    }

    override fun confirmStocks(orderId: Long, quantitiesByProductId: Map<Long, Int>): Long? =
        transitionReservation(orderId, quantitiesByProductId, StockLuaScripts.CONFIRMED_STATE)

    override fun releaseStocks(orderId: Long, quantitiesByProductId: Map<Long, Int>): Long? =
        transitionReservation(orderId, quantitiesByProductId, StockLuaScripts.RELEASED_STATE)

    override fun releaseStocksForTransaction(orderId: Long, quantities: Map<Long, Int>, attemptId: String): Long? =
        transitionReservation(orderId, quantities, StockLuaScripts.RELEASED_STATE, attemptId)

    private fun transitionReservation(
        orderId: Long,
        quantities: Map<Long, Int>,
        target: String,
        attemptId: String = "",
    ): Long? {
        if (quantities.isEmpty()) return null
        val entries = quantities.toSortedMap().entries.toList()
        val keys = listOf(getReservationKey(orderId), DIRTY_SET_KEY, StockLuaScripts.PENDING_RESERVATIONS_KEY) +
            entries.flatMap { listOf(getReservedKey(it.key), getConfirmedKey(it.key)) }
        val items = serializeItems(entries)
        val args = entries.flatMap { listOf(it.value.toString(), it.key.toString()) }.toTypedArray()
        val result = redisTemplate.execute(StockLuaScripts.TRANSITION_RESERVATION_SCRIPT, keys, items, target, attemptId, *args)
        return failedProductId(orderId, result, entries)
    }

    override fun getAvailableStock(productId: Long): Int {
        val key = getAvailableKey(productId)
        return redisTemplate.opsForValue()[key]?.toInt() ?: 0
    }

    override fun getReservedStock(productId: Long): Int {
        val key = getReservedKey(productId)
        return redisTemplate.opsForValue()[key]?.toInt() ?: 0
    }

    override fun getConfirmedStock(productId: Long): Int {
        val key = getConfirmedKey(productId)
        return redisTemplate.opsForValue()[key]?.toInt() ?: 0
    }

    override fun setStock(
        productId: Long,
        stock: Int,
    ) {
        val key = getAvailableKey(productId)
        redisTemplate.opsForValue()[key] = stock.toString()
    }

    override fun setStockBatch(stockMap: Map<Long, Int>) {
        if (stockMap.isEmpty()) {
            return
        }

        val entries = stockMap.entries.toList()
        val keys = entries.flatMap { listOf(getAvailableKey(it.key), getReservedKey(it.key), getConfirmedKey(it.key)) }
        redisTemplate.execute(StockLuaScripts.INITIALIZE_STOCK_SCRIPT, keys, *entries.map { it.value.toString() }.toTypedArray())
    }

    override fun getReservationsForRecovery(nowMillis: Long, limit: Int): List<StockReservationSnapshot> {
        val ids = redisTemplate.opsForZSet().rangeByScore(
            StockLuaScripts.PENDING_RESERVATIONS_KEY, Double.NEGATIVE_INFINITY, nowMillis.toDouble(), 0, limit.toLong(),
        ).orEmpty()
        return ids.mapNotNull { id ->
            val fields = redisTemplate.opsForHash<String, String>().entries(getReservationKey(id.toLong()))
            if (fields[StockLuaScripts.STATUS_FIELD] != StockLuaScripts.RESERVED_STATE) {
                redisTemplate.opsForZSet().remove(StockLuaScripts.PENDING_RESERVATIONS_KEY, id)
                null
            } else {
                val quantities = deserializeItems(fields.getValue(StockLuaScripts.ITEMS_FIELD))
                StockReservationSnapshot(id.toLong(), quantities)
            }
        }
    }

    override fun deferReservationRecovery(orderId: Long, retryAtMillis: Long) {
        redisTemplate.execute(
            StockLuaScripts.DEFER_RECOVERY_SCRIPT,
            listOf(getReservationKey(orderId), StockLuaScripts.PENDING_RESERVATIONS_KEY),
            orderId.toString(),
            retryAtMillis.toString(),
        )
    }

    override fun rebuildReservationRecoveryIndex() {
        val options = ScanOptions.scanOptions()
            .match("${StockLuaScripts.RESERVATION_KEY_PREFIX}*")
            .count(RESERVATION_SCAN_COUNT)
            .build()
        redisTemplate.scan(options).use { cursor ->
            while (cursor.hasNext()) {
                val key = cursor.next()
                redisTemplate.execute(
                    StockLuaScripts.REINDEX_RESERVATION_SCRIPT,
                    listOf(key, StockLuaScripts.PENDING_RESERVATIONS_KEY),
                    key.substringAfterLast(':'),
                )
            }
        }
    }

    override fun getDirtyStockSnapshot(): Map<Long, StockSyncSnapshot> {
        val ids = redisTemplate.opsForSet().members(DIRTY_SET_KEY).orEmpty().map { it.toLong() }
        return ids.associateWith { id ->
            val values = redisTemplate.execute(StockLuaScripts.STOCK_SNAPSHOT_SCRIPT, listOf(getAvailableKey(id), getConfirmedKey(id)))
            StockSyncSnapshot((values[0] as Long).toInt(), (values[1] as Long).toInt())
        }
    }

    override fun acknowledgeStockSync(snapshot: Map<Long, StockSyncSnapshot>) {
        snapshot.forEach { (id, value) ->
            redisTemplate.execute(StockLuaScripts.ACK_STOCK_SYNC_SCRIPT, listOf(DIRTY_SET_KEY, getConfirmedKey(id)), id.toString(), value.confirmed.toString())
        }
    }

    override fun getStock(productId: Long): Int {
        val available = getAvailableStock(productId)
        val reserved = getReservedStock(productId)
        val confirmed = getConfirmedStock(productId)
        return available - reserved - confirmed
    }
}
