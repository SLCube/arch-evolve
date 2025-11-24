package com.playground.payment.persistence.repository

import com.playground.payment.persistence.entity.PaymentJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<PaymentJpaEntity, Long> {
}