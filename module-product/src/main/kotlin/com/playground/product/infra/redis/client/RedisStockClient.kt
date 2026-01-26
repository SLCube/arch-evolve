package com.playground.product.infra.redis.client

import com.playground.product.application.port.outbound.StockCachePort
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component

@Component
class RedisStockClient(
    private val redisTemplate: RedisTemplate<String, String>,
) : StockCachePort {
    companion object {
        private const val STOCK_KEY_PREFIX = "product:stock:"
        private const val DIRTY_SET_KEY = "product:stock:dirty"

        /**
         * Lua Script: 재고 차감 + 더티 플래그 추가 (Atomic)
         *
         * KEYS[1]: product:stock:{productId}
         * KEYS[2]: product:stock:dirty
         * ARGV[1]: quantity
         * ARGV[2]: productId
         *
         * 반환: 성공 시 남은 재고, 실패 시 -1
         */
        private const val DECREASE_AND_MARK_DIRTY_SCRIPT =
            """
local stockKey = KEYS[1]
local dirtySetKey = KEYS[2]
local quantity = tonumber(ARGV[1])
local productId = ARGV[2]

local stock = tonumber(redis.call('GET', stockKey) or '0')

if stock >= quantity then
    local remaining = redis.call('DECRBY', stockKey, quantity)
    redis.call('SADD', dirtySetKey, productId)
    return remaining
else
    return -1
end
            """

        // Script 객체 재사용 (GC 부담 감소)
        private val DECREASE_STOCK_SCRIPT: DefaultRedisScript<Long> =
            DefaultRedisScript<Long>().apply {
                setScriptText(DECREASE_AND_MARK_DIRTY_SCRIPT)
                resultType = Long::class.java
            }
    }

    private fun getStockKey(productId: Long): String = "$STOCK_KEY_PREFIX$productId"

    override fun decreaseStock(
        productId: Long,
        quantity: Int,
    ): Long {
        val stockKey = getStockKey(productId)

        return redisTemplate.execute(
            DECREASE_STOCK_SCRIPT,
            listOf(stockKey, DIRTY_SET_KEY),
            quantity.toString(),
            productId.toString(),
        )
    }

    override fun setStock(
        productId: Long,
        stock: Int,
    ) {
        val key = getStockKey(productId)
        redisTemplate.opsForValue()[key] = stock.toString()
    }

    override fun setStockBatch(stockMap: Map<Long, Int>) {
        if (stockMap.isEmpty()) {
            return
        }

        val redisMap =
            stockMap
                .mapKeys { getStockKey(it.key) }
                .mapValues { it.value.toString() }

        redisTemplate.opsForValue().multiSet(redisMap)
    }

    override fun getStock(productId: Long): Int {
        val key = getStockKey(productId)
        return redisTemplate.opsForValue()[key]?.toInt() ?: 0
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
