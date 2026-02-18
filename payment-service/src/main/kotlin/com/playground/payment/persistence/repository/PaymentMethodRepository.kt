package com.playground.payment.persistence.repository

import com.playground.payment.persistence.entity.PaymentMethodJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentMethodRepository :
    JpaRepository<PaymentMethodJpaEntity, Long>,
    PaymentMethodQueryRepository {
    fun findAllByUserId(userId: Long): List<PaymentMethodJpaEntity>

    fun countByUserId(userId: Long): Long
}
