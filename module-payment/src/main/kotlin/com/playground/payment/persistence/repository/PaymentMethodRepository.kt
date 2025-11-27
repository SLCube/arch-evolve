package com.playground.payment.persistence.repository

import com.playground.payment.persistence.entity.PaymentMethodJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PaymentMethodRepository : JpaRepository<PaymentMethodJpaEntity, Long> {
    fun findByUserId(userId: Long): Optional<PaymentMethodJpaEntity>
    fun findAllByUserId(userId: Long): List<PaymentMethodJpaEntity>
}