package com.playground.payment.application.service

import com.playground.payment.application.port.inbound.PaymentUseCase
import com.playground.payment.application.port.inbound.command.PaymentAuthorizeCommand
import com.playground.payment.application.port.outbound.PaymentCommandPort
import com.playground.payment.application.port.outbound.PaymentEventPort
import com.playground.payment.application.port.outbound.PaymentGatewayPort
import com.playground.payment.application.port.outbound.PaymentMethodQueryPort
import com.playground.payment.application.port.outbound.PaymentQueryPort
import com.playground.payment.contract.domain.event.PaymentCompletedEvent
import com.playground.payment.contract.domain.event.PaymentFailedEvent
import com.playground.payment.domain.enum.PaymentStatus
import com.playground.payment.domain.exception.PaymentFailedException
import com.playground.payment.domain.model.Payment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PaymentService(
    private val paymentQueryPort: PaymentQueryPort,
    private val paymentCommandPort: PaymentCommandPort,
    private val paymentGatewayPort: PaymentGatewayPort,
    private val paymentEventPort: PaymentEventPort,
    private val paymentMethodQueryPort: PaymentMethodQueryPort,
) : PaymentUseCase {
    override fun authorizePayment(command: PaymentAuthorizeCommand): Payment {
        paymentQueryPort.findByOrderId(command.orderId)?.let { existingPayment ->
            return existingPayment
        }

        val paymentMethod = paymentMethodQueryPort.findDefaultByUserId(command.userId)

        val payment = Payment(
            userId = command.userId,
            orderId = command.orderId,
            amount = command.amount,
            usedPaymentKey = paymentMethod.billingKey,
            status = PaymentStatus.PENDING,
        )

        val pgResult = paymentGatewayPort.requestAuthorization(paymentMethod.billingKey, command.amount)

        if (pgResult.isSuccess) {
            payment.complete(pgResult.requirePgTransactionId(), pgResult.requireApprovalNumber())
            val savedPayment = paymentCommandPort.save(payment)
            paymentEventPort.publish(
                PaymentCompletedEvent(
                    orderId = savedPayment.orderId,
                    userId = savedPayment.userId,
                    amount = savedPayment.amount,
                    pgTransactionId = savedPayment.pgTransactionId!!
                )
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
                )
            )

            throw PaymentFailedException(pgResult.failReason)
        }
    }
}