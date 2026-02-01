package com.playground.product.infra.redis.client

import com.playground.product.application.port.outbound.StockCachePort
import com.playground.product.infra.redis.client.StockLuaScripts.AVAILABLE_KEY_SUFFIX
import com.playground.product.infra.redis.client.StockLuaScripts.CONFIRMED_KEY_SUFFIX
import com.playground.product.infra.redis.client.StockLuaScripts.CONFIRM_STOCK_SCRIPT
import com.playground.product.infra.redis.client.StockLuaScripts.DIRTY_SET_KEY
import com.playground.product.infra.redis.client.StockLuaScripts.RELEASE_RESERVED_STOCK_SCRIPT
import com.playground.product.infra.redis.client.StockLuaScripts.RESERVED_KEY_SUFFIX
import com.playground.product.infra.redis.client.StockLuaScripts.RESERVE_STOCK_SCRIPT
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

    // 3단계 재고 관리 구현
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

    override fun getDirtyProductIds(): Set<Long> =
        (redisTemplate.opsForSet().members(DIRTY_SET_KEY) ?: emptySet())
            .map { it.toLong() }
            .toSet()

    override fun removeDirtyFlags(productIds: Set<Long>) {
        if (productIds.isEmpty()) {
            return
        }

        redisTemplate.opsForSet().remove(
            DIRTY_SET_KEY,
            *productIds.map { it.toString() }.toTypedArray(),
        )
    }
}
