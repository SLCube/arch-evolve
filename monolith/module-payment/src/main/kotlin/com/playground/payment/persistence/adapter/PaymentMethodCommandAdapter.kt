package com.playground.payment.persistence.adapter

import com.playground.payment.application.port.outbound.PaymentMethodCommandPort
import com.playground.payment.domain.model.PaymentMethod
import com.playground.payment.persistence.entity.PaymentMethodJpaEntity
import com.playground.payment.persistence.mapper.toDomain
import com.playground.payment.persistence.repository.PaymentMethodRepository
import org.springframework.stereotype.Component

@Component
class PaymentMethodCommandAdapter(
    private val paymentMethodRepository: PaymentMethodRepository,
) : PaymentMethodCommandPort {
    override fun save(paymentMethod: PaymentMethod): PaymentMethod {
        val paymentMethodJpaEntity = PaymentMethodJpaEntity.toJpaEntity(paymentMethod)
        return paymentMethodRepository.save(paymentMethodJpaEntity)
            .toDomain()
    }

    override fun delete(paymentMethod: PaymentMethod) {
        val paymentMethodJpaEntity = PaymentMethodJpaEntity.toJpaEntity(paymentMethod)
        paymentMethodRepository.delete(paymentMethodJpaEntity)
    }
}