package com.playground.payment.application.service

import com.playground.payment.application.port.inbound.PaymentUseCase
import com.playground.payment.application.port.inbound.command.PaymentAuthorizeCommand
import com.playground.payment.application.port.outbound.PaymentGatewayPort
import com.playground.payment.application.support.PaymentTransactionManager
import com.playground.payment.common.log.utils.logger
import com.playground.payment.domain.model.Payment
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val paymentTransactionManager: PaymentTransactionManager,
    private val paymentGatewayPort: PaymentGatewayPort,
) : PaymentUseCase {
    private val log = logger()

    override fun authorizePayment(command: PaymentAuthorizeCommand): Payment {
        paymentTransactionManager.findByOrderId(command.orderId)?.let { existingPayment ->
            log.info(
                "이미 처리된 결제 [orderId={}, paymentId={}, status={}]",
                existingPayment.orderId,
                existingPayment.id,
                existingPayment.status,
            )
            return existingPayment
        }

        val paymentMethod = paymentTransactionManager.getPaymentMethod(command.userId)

        val pgResult = paymentGatewayPort.requestAuthorization(paymentMethod.billingKey, command.amount)

        return paymentTransactionManager.savePaymentResult(command, pgResult, paymentMethod.billingKey).also {
            log.info("결제 처리 완료 [orderId={}, paymentId={}, status={}]", it.orderId, it.id, it.status)
        }
    }
}
