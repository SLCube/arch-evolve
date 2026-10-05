package com.playground.product.infra.redis.client

import com.playground.product.application.port.outbound.StockCachePort
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
    ): Long? {
        if (quantitiesByProductId.isEmpty()) {
            return null
        }

        val entries = quantitiesByProductId.toSortedMap().entries.toList()
        val keys =
            listOf("${STOCK_KEY_PREFIX}reservation:$orderId") + entries.flatMap { (productId, _) ->
                listOf(getAvailableKey(productId), getReservedKey(productId), getConfirmedKey(productId))
            }
        val items = entries.joinToString(";") { (productId, quantity) -> "$productId:$quantity" }
        val quantities = entries.map { it.value.toString() }.toTypedArray()
        val failedIndex = redisTemplate.execute(RESERVE_STOCKS_SCRIPT, keys, items, *quantities)
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

    private fun transitionReservation(orderId: Long, quantities: Map<Long, Int>, target: String): Long? {
        if (quantities.isEmpty()) return null
        val entries = quantities.toSortedMap().entries.toList()
        val keys = listOf("${STOCK_KEY_PREFIX}reservation:$orderId", DIRTY_SET_KEY) +
            entries.flatMap { listOf(getReservedKey(it.key), getConfirmedKey(it.key)) }
        val items = entries.joinToString(";") { (id, quantity) -> "$id:$quantity" }
        val args = entries.flatMap { listOf(it.value.toString(), it.key.toString()) }.toTypedArray()
        val result = redisTemplate.execute(StockLuaScripts.TRANSITION_RESERVATION_SCRIPT, keys, items, target, *args)
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

        val availableMap =
            stockMap
                .mapKeys { getAvailableKey(it.key) }
                .mapValues { it.value.toString() }
        redisTemplate.opsForValue().multiSet(availableMap)

        val confirmedMap =
            stockMap.keys.associateBy(
                { getConfirmedKey(it) },
                { "0" },
            )
        redisTemplate.opsForValue().multiSet(confirmedMap)

        val reservedMap =
            stockMap.keys.associateBy(
                { getReservedKey(it) },
                { "0" },
            )
        redisTemplate.opsForValue().multiSet(reservedMap)
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
