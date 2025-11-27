package com.playground.payment.persistence.adapter

import com.playground.payment.application.port.outbound.PaymentMethodQueryPort
import com.playground.payment.domain.exception.PaymentMethodNotFoundException
import com.playground.payment.domain.model.PaymentMethod
import com.playground.payment.persistence.mapper.toDomain
import com.playground.payment.persistence.repository.PaymentMethodRepository
import org.springframework.stereotype.Component

@Component
class PaymentMethodQueryAdapter(
    private val paymentMethodRepository: PaymentMethodRepository,
) : PaymentMethodQueryPort {
    override fun findByUserId(userId: Long): PaymentMethod {
        return paymentMethodRepository.findByUserId(userId)
            .orElseThrow { PaymentMethodNotFoundException(userId) }
            .toDomain()
    }

    override fun findAllByUserId(userId: Long): List<PaymentMethod> {
        return paymentMethodRepository.findAllByUserId(userId)
            .map { it.toDomain() }
    }
}