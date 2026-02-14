package com.playground.payment.persistence.adapter

import com.playground.payment.application.port.outbound.PaymentQueryPort
import com.playground.payment.domain.model.Payment
import com.playground.payment.persistence.mapper.toDomain
import com.playground.payment.persistence.repository.PaymentRepository
import org.springframework.stereotype.Component

@Component
class PaymentQueryAdapter(
    private val paymentRepository: PaymentRepository,
) : PaymentQueryPort {
    override fun findByOrderId(orderId: Long): Payment? {
        return paymentRepository.findByOrderId(orderId)
            .map { it.toDomain() }
            .orElse(null)
    }
}