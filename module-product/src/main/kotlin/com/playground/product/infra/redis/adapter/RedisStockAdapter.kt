package com.playground.product.infra.redis.adapter

import com.playground.product.application.port.outbound.ProductCommandPort
import com.playground.product.domain.exception.InsufficientStockException
import com.playground.product.domain.exception.ProductNotFoundException
import com.playground.product.domain.model.Product
import com.playground.product.infra.redis.service.RedisStockService
import com.playground.product.persistence.entity.ProductJpaEntity
import com.playground.product.persistence.mapper.toDomain
import com.playground.product.persistence.repository.ProductRepository
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

/**
 * Redis 기반 재고 관리 Adapter
 *
 * ProductCommandPort 구현체로, @Primary 어노테이션을 통해
 * 기존 ProductCommandAdapter 대신 사용됩니다.
 *
 * - save, update: DB 직접 처리 (기존과 동일)
 * - decreaseStock: Redis 기반 처리 (성능 최적화)
 */
@Primary
@Component
class RedisStockAdapter(
    private val productRepository: ProductRepository,
    private val redisStockService: RedisStockService,
) : ProductCommandPort {
    override fun save(product: Product): Product {
        val productJpaEntity = ProductJpaEntity.toJpaEntity(product)
        val savedEntity = productRepository.save(productJpaEntity)
        return savedEntity.toDomain()
    }

    override fun update(product: Product): Product {
        val productId = product.id!!

        val productJpaEntity =
            productRepository
                .findById(productId)
                .orElseThrow { ProductNotFoundException(productId) }

        productJpaEntity.updateFromDomain(product)

        return productJpaEntity.toDomain()
    }

    /**
     * Redis 기반 재고 차감
     *
     * Lua Script를 통해 Atomic하게 재고를 차감하고,
     * 더티 플래그를 추가합니다. 실제 DB 업데이트는
     * StockSyncScheduler에서 배치로 처리됩니다.
     *
     * @param product 상품
     * @param quantity 차감할 수량
     * @return 남은 재고 수량
     * @throws InsufficientStockException 재고 부족 시
     */
    override fun decreaseStock(
        product: Product,
        quantity: Int,
    ): Long {
        val productId = product.id!!

        val remaining = redisStockService.decreaseStockIfAvailable(productId, quantity)

        if (remaining < 0) {
            throw InsufficientStockException(productId, quantity)
        }

        return remaining
    }
}
