package com.playground.product.infra.redis.service

import com.playground.common.log.utils.logger
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Service

@Service
class RedisStockService(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    private val logger = logger()

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

        /**
         * 재사용 가능한 Redis Script 인스턴스
         * 매 요청마다 객체를 생성하지 않고 캐싱하여 사용 (GC 부담 감소)
         */
        private val DECREASE_STOCK_SCRIPT: DefaultRedisScript<Long> =
            DefaultRedisScript<Long>().apply {
                setScriptText(DECREASE_AND_MARK_DIRTY_SCRIPT)
                resultType = Long::class.java
            }
    }

    /**
     * Redis 재고 Key 생성
     */
    private fun getStockKey(productId: Long): String = "$STOCK_KEY_PREFIX$productId"

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
        val stockKey = getStockKey(productId)

        val result: Long =
            redisTemplate.execute(
                DECREASE_STOCK_SCRIPT,
                listOf(stockKey, DIRTY_SET_KEY),
                quantity.toString(),
                productId.toString(),
            )

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
        val key = getStockKey(productId)
        redisTemplate.opsForValue()[key] = stock.toString()
        logger.debug("재고 설정: productId=$productId, stock=$stock")
    }

    /**
     * 재고 일괄 설정 (MSET 사용)
     *
     * Redis MSET 명령어를 사용하여 대량의 재고 데이터를 한 번에 설정합니다.
     * 네트워크 왕복을 최소화하여 성능을 크게 개선합니다.
     *
     * @param stockMap 상품 ID -> 재고 수량 맵
     */
    fun setStockBatch(stockMap: Map<Long, Int>) {
        if (stockMap.isEmpty()) {
            return
        }

        val redisMap =
            stockMap
                .mapKeys { getStockKey(it.key) }
                .mapValues { it.value.toString() }

        redisTemplate.opsForValue().multiSet(redisMap)

        logger.debug("재고 일괄 설정 완료: ${stockMap.size}개 상품")
    }

    /**
     * 재고 조회
     *
     * @param productId 상품 ID
     * @return 현재 재고 수량 (없으면 0)
     */
    fun getStock(productId: Long): Int {
        val key = getStockKey(productId)
        return redisTemplate.opsForValue()[key]?.toInt() ?: 0
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
     * 더티 플래그 초기화 (전체 삭제)
     * 배치 동기화 완료 후 호출
     */
    fun clearDirtyFlags() {
        redisTemplate.delete(DIRTY_SET_KEY)
        logger.debug("더티 플래그 초기화 완료")
    }

    /**
     * 특정 상품들의 더티 플래그만 제거
     *
     * 동시성 안전을 위해 처리 완료된 상품만 개별 제거합니다.
     *
     * @param productIds 제거할 상품 ID 집합
     */
    fun removeDirtyFlags(productIds: Set<Long>) {
        if (productIds.isEmpty()) {
            return
        }

        val removed =
            redisTemplate.opsForSet().remove(
                DIRTY_SET_KEY,
                *productIds.map { it.toString() }.toTypedArray(),
            ) ?: 0

        logger.debug("더티 플래그 제거: ${productIds.size}개 요청, ${removed}개 제거됨")
    }
}
