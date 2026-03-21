package com.playground.payment.persistence.adapter

import com.playground.payment.application.port.outbound.PaymentMethodQueryPort
import com.playground.payment.domain.exception.DefaultPaymentMethodNotFoundException
import com.playground.payment.domain.exception.PaymentMethodNotFoundException
import com.playground.payment.domain.model.PaymentMethod
import com.playground.payment.persistence.mapper.toDomain
import com.playground.payment.persistence.repository.PaymentMethodRepository
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class PaymentMethodQueryAdapter(
    private val paymentMethodRepository: PaymentMethodRepository,
) : PaymentMethodQueryPort {
    override fun getDefaultByUserId(userId: Long): PaymentMethod =
        paymentMethodRepository
            .findDefaultByUserId(userId)
            .orElseThrow { DefaultPaymentMethodNotFoundException(userId) }
            .toDomain()

    override fun findDefaultOrNullByUserId(userId: Long): Optional<PaymentMethod> =
        paymentMethodRepository
            .findDefaultByUserId(userId)
            .map { it.toDomain() }

    override fun findAllByUserId(userId: Long): List<PaymentMethod> =
        paymentMethodRepository
            .findAllByUserId(userId)
            .map { it.toDomain() }

    override fun getById(paymentMethodId: Long): PaymentMethod =
        paymentMethodRepository
            .findById(paymentMethodId)
            .orElseThrow { PaymentMethodNotFoundException(paymentMethodId) }
            .toDomain()

    override fun countByUserId(userId: Long): Long = paymentMethodRepository.countByUserId(userId)
}
