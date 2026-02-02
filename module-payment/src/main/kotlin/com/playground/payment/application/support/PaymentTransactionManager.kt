package com.playground.payment.application.support

import com.playground.payment.application.port.inbound.command.PaymentAuthorizeCommand
import com.playground.payment.application.port.outbound.PaymentCommandPort
import com.playground.payment.application.port.outbound.PaymentEventPort
import com.playground.payment.application.port.outbound.PaymentMethodQueryPort
import com.playground.payment.application.port.outbound.PaymentQueryPort
import com.playground.payment.contract.domain.event.PaymentCompletedEvent
import com.playground.payment.contract.domain.event.PaymentFailedEvent
import com.playground.payment.domain.enum.PaymentStatus
import com.playground.payment.domain.exception.PaymentFailedException
import com.playground.payment.domain.model.Payment
import com.playground.payment.domain.model.PaymentMethod
import com.playground.payment.domain.vo.PgAuthorizationResult
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class PaymentTransactionManager(
    private val paymentQueryPort: PaymentQueryPort,
    private val paymentCommandPort: PaymentCommandPort,
    private val paymentEventPort: PaymentEventPort,
    private val paymentMethodQueryPort: PaymentMethodQueryPort,
) {
    @Transactional(readOnly = true)
    fun findByOrderId(orderId: Long): Payment? {
        return paymentQueryPort.findByOrderId(orderId)
    }

    @Transactional(readOnly = true)
    fun getPaymentMethod(userId: Long): PaymentMethod {
        return paymentMethodQueryPort.findDefaultByUserId(userId)
    }

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

        if (pgResult.isSuccess) {
            payment.complete(pgResult.requirePgTransactionId(), pgResult.requireApprovalNumber())
            val savedPayment = paymentCommandPort.save(payment)
            paymentEventPort.publish(
                PaymentCompletedEvent(
                    orderId = savedPayment.orderId,
                    userId = savedPayment.userId,
                    amount = savedPayment.amount,
                    pgTransactionId = savedPayment.pgTransactionId!!,
                ),
            )
            return savedPayment
        } else {
            payment.fail(pgResult.failReason)
            paymentCommandPort.save(payment)

            paymentEventPort.publish(
                PaymentFailedEvent(
                    orderId = command.orderId,
                    userId = command.userId,
                    failReason = pgResult.failReason,
                ),
            )

            throw PaymentFailedException(pgResult.failReason)
        }
    }
}
