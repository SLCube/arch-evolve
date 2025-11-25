package com.playground.payment.persistence.repository

import com.playground.payment.persistence.entity.PaymentJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface PaymentRepository : JpaRepository<PaymentJpaEntity, Long> {
    fun findByOrderId(orderId: Long) : Optional<PaymentJpaEntity>
}