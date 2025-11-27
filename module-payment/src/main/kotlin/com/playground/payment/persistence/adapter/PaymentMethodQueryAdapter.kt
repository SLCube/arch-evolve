package com.playground.payment.persistence.adapter

import com.playground.payment.application.port.outbound.PaymentMethodQueryPort
import com.playground.payment.domain.exception.DefaultPaymentMethodNotFoundException
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
            .orElseThrow { DefaultPaymentMethodNotFoundException(userId) }
            .toDomain()
    }

    override fun findAllByUserId(userId: Long): List<PaymentMethod> {
        return paymentMethodRepository.findAllByUserId(userId)
            .map { it.toDomain() }
    }

    override fun findById(paymentMethodId: Long): PaymentMethod {
        return paymentMethodRepository.findById(paymentMethodId)
            .orElseThrow { PaymentMethodNotFoundException(paymentMethodId) }
            .toDomain()
    }
}