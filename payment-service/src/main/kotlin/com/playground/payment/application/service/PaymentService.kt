package com.playground.payment.application.service

import com.playground.payment.application.port.inbound.PaymentUseCase
import com.playground.payment.application.port.inbound.command.PaymentAuthorizeCommand
import com.playground.payment.application.port.outbound.PaymentGatewayPort
import com.playground.payment.application.support.PaymentTransactionManager
import com.playground.payment.domain.model.Payment
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val paymentTransactionManager: PaymentTransactionManager,
    private val paymentGatewayPort: PaymentGatewayPort,
) : PaymentUseCase {
    override fun authorizePayment(command: PaymentAuthorizeCommand): Payment {
        paymentTransactionManager.findByOrderId(command.orderId)?.let { existingPayment ->
            return existingPayment
        }

        val paymentMethod = paymentTransactionManager.getPaymentMethod(command.userId)

        val pgResult = paymentGatewayPort.requestAuthorization(paymentMethod.billingKey, command.amount)

        return paymentTransactionManager.savePaymentResult(command, pgResult, paymentMethod.billingKey)
    }
}
