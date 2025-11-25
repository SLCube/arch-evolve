package com.playground.payment.persistence.adapter

import com.playground.payment.application.port.outbound.PaymentCommandPort
import com.playground.payment.domain.model.Payment
import com.playground.payment.persistence.entity.PaymentJpaEntity
import com.playground.payment.persistence.mapper.toDomain
import com.playground.payment.persistence.repository.PaymentRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class PaymentCommandAdapter(
    private val paymentRepository: PaymentRepository,
) : PaymentCommandPort {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun save(payment: Payment): Payment {
        val paymentJpaEntity = PaymentJpaEntity.toJpaEntity(payment)
        val savedPaymentJpaEntity = paymentRepository.save(paymentJpaEntity)
        return savedPaymentJpaEntity.toDomain()
    }
}