package com.playground.payment.persistence.repository

import com.playground.payment.persistence.entity.PaymentMethodJpaEntity
import com.querydsl.jpa.impl.JPAQueryFactory
import java.util.Optional
import com.playground.payment.persistence.entity.QPaymentMethodJpaEntity.Companion.paymentMethodJpaEntity as paymentMethod

class PaymentMethodQueryRepositoryImpl(
    private val queryFactory: JPAQueryFactory,
) : PaymentMethodQueryRepository {
    override fun findDefaultByUserId(userId: Long): Optional<PaymentMethodJpaEntity> {
        val entity =
            queryFactory
                .selectFrom(paymentMethod)
                .where(
                    paymentMethod.userId.eq(userId),
                    paymentMethod.isDefault.isTrue,
                ).fetchOne()

        return Optional.ofNullable(entity)
    }
}
