package com.playground.product.persistence.repository

import com.playground.product.persistence.entity.QProductJpaEntity.Companion.productJpaEntity
import com.querydsl.jpa.impl.JPAQueryFactory

class ProductCustomRepositoryImpl(
    private val queryFactory: JPAQueryFactory
): ProductCustomRepository {

    override fun decreaseStockAtomic(productId: Long, quantity: Int): Long {
        return queryFactory.update(productJpaEntity)
            .set(productJpaEntity.stock, productJpaEntity.stock.subtract(quantity))
            .where(
                productJpaEntity.id.eq(productId),
                productJpaEntity.stock.goe(quantity)
            )
            .execute()
    }
}