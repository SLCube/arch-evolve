package com.playground.product.infra.redis.service

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service

@Service
class RedisStockService(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val STOCK_KEY_PREFIX = "product:stock:"
        private const val DIRTY_SET_KEY = "product:stock:dirty"

        /**
         * 재고 차감 + 더티 플래그 추가 Lua Script
         *
         * KEYS[1]: product:stock:{productId}
         * KEYS[2]: product:stock:dirty
         * ARGV[1]: quantity (차감할 수량)
         * ARGV[2]: productId (더티 세트에 추가할 ID)
         *
         * 반환값: 성공 시 남은 재고, 실패 시 -1
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
    }

    /**
     * 재고 차감 (Atomic)
     *
     * @param productId 상품 ID
     * @param quantity 차감할 수량
     * @return 성공 시 남은 재고 수량, 재고 부족 시 -1
     */
    fun decreaseStockIfAvailable(
        productId: Long,
        quantity: Int,
    ): Long {
        val stockKey = "$STOCK_KEY_PREFIX$productId"

        val script = DefaultRedisScript<Long>()
        script.setScriptText(DECREASE_AND_MARK_DIRTY_SCRIPT)
        script.resultType = Long::class.java

        val result: Long =
            redisTemplate.execute(
                script,
                listOf(stockKey, DIRTY_SET_KEY),
                quantity.toString(),
                productId.toString(),
            ) ?: -1

        if (result >= 0) {
            logger.debug("재고 차감 성공: productId=$productId, quantity=$quantity, remaining=$result")
        } else {
            logger.warn("재고 부족: productId=$productId, requested=$quantity")
        }

        return result
    }

    /**
     * 재고 설정 (초기화 또는 동기화 시 사용)
     *
     * @param productId 상품 ID
     * @param stock 설정할 재고 수량
     */
    fun setStock(
        productId: Long,
        stock: Int,
    ) {
        val key = "$STOCK_KEY_PREFIX$productId"
        redisTemplate.opsForValue().set(key, stock.toString())
        logger.debug("재고 설정: productId=$productId, stock=$stock")
    }

    /**
     * 재고 조회
     *
     * @param productId 상품 ID
     * @return 현재 재고 수량 (없으면 0)
     */
    fun getStock(productId: Long): Int {
        val key = "$STOCK_KEY_PREFIX$productId"
        return redisTemplate.opsForValue().get(key)?.toInt() ?: 0
    }

    /**
     * 변경된 상품 ID 목록 조회 (더티 플래그)
     *
     * @return 재고가 변경된 상품 ID 집합
     */
    fun getDirtyProductIds(): Set<Long> {
        val dirtyIds =
            (redisTemplate.opsForSet().members(DIRTY_SET_KEY) ?: emptySet())
                .map { it.toLong() }
                .toSet()

        logger.debug("더티 플래그 조회: ${dirtyIds.size}개 상품")
        return dirtyIds
    }

    /**
     * 더티 플래그 초기화
     * 배치 동기화 완료 후 호출
     */
    fun clearDirtyFlags() {
        redisTemplate.delete(DIRTY_SET_KEY)
        logger.debug("더티 플래그 초기화 완료")
    }
}
