package com.playground.payment.application.support

import com.playground.payment.application.port.inbound.command.PaymentAuthorizeCommand
import com.playground.payment.application.port.outbound.OutboxCommandPort
import com.playground.payment.application.port.outbound.OutboxEventPublisherPort
import com.playground.payment.application.port.outbound.PaymentCommandPort
import com.playground.payment.application.port.outbound.PaymentMethodQueryPort
import com.playground.payment.application.port.outbound.PaymentQueryPort
import com.playground.payment.domain.enum.PaymentStatus
import com.playground.payment.domain.model.Payment
import com.playground.payment.domain.model.PaymentMethod
import com.playground.payment.domain.vo.PgAuthorizationResult
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PaymentTransactionManager(
    private val paymentQueryPort: PaymentQueryPort,
    private val paymentCommandPort: PaymentCommandPort,
    private val paymentMethodQueryPort: PaymentMethodQueryPort,
    private val outboxCommandPort: OutboxCommandPort,
    private val outboxFactory: OutboxFactory,
    private val eventPublisher: OutboxEventPublisherPort,
) {
    @Transactional(readOnly = true)
    fun findByOrderId(orderId: Long): Payment? = paymentQueryPort.findByOrderId(orderId)

    @Transactional(readOnly = true)
    fun getPaymentMethod(userId: Long): PaymentMethod = paymentMethodQueryPort.findDefaultByUserId(userId)

    @Transactional
    fun savePaymentResult(
        command: PaymentAuthorizeCommand,
        pgResult: PgAuthorizationResult,
        billingKey: String,
    ): Payment {
        val payment =
            Payment(
                userId = command.userId,
                orderId = command.orderId,
                amount = command.amount,
                usedPaymentKey = billingKey,
                status = PaymentStatus.PENDING,
            )

        val domainEvent =
            if (pgResult.isSuccess) {
                payment.complete(pgResult.requirePgTransactionId(), pgResult.requireApprovalNumber())
            } else {
                payment.fail(pgResult.failReason)
            }
        val savedPayment = paymentCommandPort.save(payment)
        val savedOutbox = outboxCommandPort.save(outboxFactory.from(domainEvent))
        eventPublisher.publish(savedOutbox)
        return savedPayment
    }
}
