package com.playground.payment.application.service

import com.playground.payment.application.port.inbound.PaymentMethodUseCase
import com.playground.payment.application.port.inbound.command.PaymentMethodDeleteCommand
import com.playground.payment.application.port.inbound.command.PaymentMethodRegisterCommand
import com.playground.payment.application.port.outbound.PaymentGatewayPort
import com.playground.payment.application.port.outbound.PaymentMethodCommandPort
import com.playground.payment.application.port.outbound.PaymentMethodQueryPort
import com.playground.payment.domain.model.PaymentMethod
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PaymentMethodService(
    private val paymentGatewayPort: PaymentGatewayPort,
    private val paymentMethodCommandPort: PaymentMethodCommandPort,
    private val paymentMethodQueryPort: PaymentMethodQueryPort,
) : PaymentMethodUseCase {
    override fun registerPaymentMethod(command: PaymentMethodRegisterCommand): PaymentMethod {
        TODO("Not yet implemented")
    }

    override fun getPaymentMethodList(userId: Long): List<PaymentMethod> {
        return paymentMethodQueryPort.findAllByUserId(userId)
    }

    override fun deletePaymentMethod(command: PaymentMethodDeleteCommand) {
        TODO("Not yet implemented")
    }
}