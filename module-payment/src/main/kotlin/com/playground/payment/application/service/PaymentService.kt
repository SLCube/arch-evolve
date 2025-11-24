package com.playground.payment.application.service

import com.playground.payment.application.port.inbound.PaymentUseCase
import com.playground.payment.application.port.inbound.command.AuthorizePaymentCommand
import com.playground.payment.application.port.outbound.PaymentCommandPort
import com.playground.payment.application.port.outbound.PaymentGatewayPort
import com.playground.payment.application.port.outbound.PaymentMethodQueryPort
import com.playground.payment.domain.enum.PaymentStatus
import com.playground.payment.domain.model.Payment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PaymentService(
    private val paymentCommandPort: PaymentCommandPort,
    private val paymentGatewayPort: PaymentGatewayPort,
    private val paymentMethodQueryPort: PaymentMethodQueryPort,
) : PaymentUseCase {
    override fun authorizePayment(command: AuthorizePaymentCommand): Payment {
        val paymentMethod = paymentMethodQueryPort.findByUserId(command.userId)

        val payment = Payment(
            userId = command.userId,
            orderId = command.orderId,
            amount = command.amount,
            usedPaymentKey = paymentMethod.paymentKey,
            status = PaymentStatus.PENDING,
        )

        val pgResult = paymentGatewayPort.requestAuthorization(paymentMethod.paymentKey, command.amount)

        if (pgResult.isSuccess) {
            payment.complete(pgResult.requirePgTransactionId(), pgResult.requireApprovalNumber())
        } else {
            payment.fail(pgResult.failReason)
        }

        paymentCommandPort.save(payment)

        return payment
    }
}