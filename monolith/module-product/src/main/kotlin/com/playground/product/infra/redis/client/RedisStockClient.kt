package com.playground.product.infra.redis.client

import com.playground.product.application.port.outbound.StockCachePort
import com.playground.product.application.port.outbound.StockSyncSnapshot
import com.playground.product.application.port.outbound.StockReservationSnapshot
import com.playground.product.domain.exception.StockReservationConflictException
import com.playground.product.domain.exception.StockReservationStateException
import com.playground.product.infra.redis.client.StockLuaScripts.AVAILABLE_KEY_SUFFIX
import com.playground.product.infra.redis.client.StockLuaScripts.CONFIRMED_KEY_SUFFIX
import com.playground.product.infra.redis.client.StockLuaScripts.CONFIRM_STOCK_SCRIPT
import com.playground.product.infra.redis.client.StockLuaScripts.DIRTY_SET_KEY
import com.playground.product.infra.redis.client.StockLuaScripts.GET_DIRTY_AND_CLEAR_SCRIPT
import com.playground.product.infra.redis.client.StockLuaScripts.RELEASE_RESERVED_STOCK_SCRIPT
import com.playground.product.infra.redis.client.StockLuaScripts.RESERVED_KEY_SUFFIX
import com.playground.product.infra.redis.client.StockLuaScripts.RESERVE_STOCK_SCRIPT
import com.playground.product.infra.redis.client.StockLuaScripts.RESERVE_STOCKS_SCRIPT
import com.playground.product.infra.redis.client.StockLuaScripts.STOCK_KEY_PREFIX
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ScanOptions
import org.springframework.stereotype.Component

@Component
class RedisStockClient(
    private val redisTemplate: RedisTemplate<String, String>,
) : StockCachePort {
    private fun getAvailableKey(productId: Long): String = "$STOCK_KEY_PREFIX$productId$AVAILABLE_KEY_SUFFIX"

    private fun getReservedKey(productId: Long): String = "$STOCK_KEY_PREFIX$productId$RESERVED_KEY_SUFFIX"

    private fun getConfirmedKey(productId: Long): String = "$STOCK_KEY_PREFIX$productId$CONFIRMED_KEY_SUFFIX"

    override fun reserveStocks(
        orderId: Long,
        quantitiesByProductId: Map<Long, Int>,
    ): Long? = reserveStocksForTransaction(orderId, quantitiesByProductId, "")

    override fun reserveStocksForTransaction(orderId: Long, quantities: Map<Long, Int>, attemptId: String): Long? {
        val quantitiesByProductId = quantities
        if (quantitiesByProductId.isEmpty()) {
            return null
        }

        val entries = quantitiesByProductId.toSortedMap().entries.toList()
        val keys =
            listOf("${STOCK_KEY_PREFIX}reservation:$orderId") + entries.flatMap { (productId, _) ->
                listOf(getAvailableKey(productId), getReservedKey(productId), getConfirmedKey(productId))
            } + StockLuaScripts.PENDING_RESERVATIONS_KEY
        val items = entries.joinToString(";") { (productId, quantity) -> "$productId:$quantity" }
        val quantities = entries.map { it.value.toString() }.toTypedArray()
        val failedIndex = redisTemplate.execute(RESERVE_STOCKS_SCRIPT, keys, items, attemptId, *quantities)
        if (failedIndex == -1L) {
            throw StockReservationConflictException(orderId)
        }
        if (failedIndex == -2L) {
            throw StockReservationStateException(orderId)
        }
        return if (failedIndex == 0L) null else entries[(failedIndex - 1).toInt()].key
    }

    override fun confirmStocks(orderId: Long, quantitiesByProductId: Map<Long, Int>): Long? =
        transitionReservation(orderId, quantitiesByProductId, "CONFIRMED")

    override fun releaseStocks(orderId: Long, quantitiesByProductId: Map<Long, Int>): Long? =
        transitionReservation(orderId, quantitiesByProductId, "RELEASED")

    override fun releaseStocksForTransaction(orderId: Long, quantities: Map<Long, Int>, attemptId: String): Long? =
        transitionReservation(orderId, quantities, "RELEASED", attemptId)

    private fun transitionReservation(
        orderId: Long,
        quantities: Map<Long, Int>,
        target: String,
        attemptId: String = "",
    ): Long? {
        if (quantities.isEmpty()) return null
        val entries = quantities.toSortedMap().entries.toList()
        val keys = listOf("${STOCK_KEY_PREFIX}reservation:$orderId", DIRTY_SET_KEY, StockLuaScripts.PENDING_RESERVATIONS_KEY) +
            entries.flatMap { listOf(getReservedKey(it.key), getConfirmedKey(it.key)) }
        val items = entries.joinToString(";") { (id, quantity) -> "$id:$quantity" }
        val args = entries.flatMap { listOf(it.value.toString(), it.key.toString()) }.toTypedArray()
        val result = redisTemplate.execute(StockLuaScripts.TRANSITION_RESERVATION_SCRIPT, keys, items, target, attemptId, *args)
        return when (result) {
            0L -> null
            -1L -> throw StockReservationConflictException(orderId)
            -2L -> throw StockReservationStateException(orderId)
            else -> entries[(result - 1).toInt()].key
        }
    }

    override fun reserveStock(
        productId: Long,
        quantity: Int,
    ): Long {
        return redisTemplate.execute(
            RESERVE_STOCK_SCRIPT,
            listOf(
                getAvailableKey(productId),
                getReservedKey(productId),
                getConfirmedKey(productId),
            ),
            quantity.toString(),
        )
    }

    override fun confirmStock(
        productId: Long,
        quantity: Int,
    ): Long {
        return redisTemplate.execute(
            CONFIRM_STOCK_SCRIPT,
            listOf(
                getReservedKey(productId),
                getConfirmedKey(productId),
                DIRTY_SET_KEY,
            ),
            quantity.toString(),
            productId.toString(),
        )
    }

    override fun releaseReservedStock(
        productId: Long,
        quantity: Int,
    ): Long {
        return redisTemplate.execute(
            RELEASE_RESERVED_STOCK_SCRIPT,
            listOf(getReservedKey(productId)),
            quantity.toString(),
        )
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
            val fields = redisTemplate.opsForHash<String, String>().entries("${STOCK_KEY_PREFIX}reservation:$id")
            if (fields["status"] != "RESERVED") {
                redisTemplate.opsForZSet().remove(StockLuaScripts.PENDING_RESERVATIONS_KEY, id)
                null
            } else {
                val quantities = fields.getValue("items").split(';').associate { item ->
                    val parts = item.split(':')
                    parts[0].toLong() to parts[1].toInt()
                }
                StockReservationSnapshot(id.toLong(), quantities)
            }
        }
    }

    override fun deferReservationRecovery(orderId: Long, retryAtMillis: Long) {
        redisTemplate.execute(StockLuaScripts.DEFER_RECOVERY_SCRIPT,
            listOf("${STOCK_KEY_PREFIX}reservation:$orderId", StockLuaScripts.PENDING_RESERVATIONS_KEY),
            orderId.toString(), retryAtMillis.toString())
    }

    override fun rebuildReservationRecoveryIndex() {
        redisTemplate.scan(ScanOptions.scanOptions().match("${STOCK_KEY_PREFIX}reservation:*").count(100).build()).use { cursor ->
            while (cursor.hasNext()) {
                val key = cursor.next()
                redisTemplate.execute(StockLuaScripts.REINDEX_RESERVATION_SCRIPT,
                    listOf(key, StockLuaScripts.PENDING_RESERVATIONS_KEY), key.substringAfterLast(':'))
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

    override fun getDirtyProductIdsAndClear(): Set<Long> {
        @Suppress("UNCHECKED_CAST")
        val result =
            redisTemplate.execute(
                GET_DIRTY_AND_CLEAR_SCRIPT,
                listOf(DIRTY_SET_KEY),
            ) as? List<String> ?: emptyList()

        return result.map { it.toLong() }.toSet()
    }
}
