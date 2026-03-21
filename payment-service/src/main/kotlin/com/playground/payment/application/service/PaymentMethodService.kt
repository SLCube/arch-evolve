package com.playground.payment.application.service

import com.playground.payment.application.port.inbound.PaymentMethodUseCase
import com.playground.payment.application.port.inbound.command.PaymentMethodDeleteCommand
import com.playground.payment.application.port.inbound.command.PaymentMethodRegisterCommand
import com.playground.payment.application.port.outbound.PaymentGatewayPort
import com.playground.payment.application.port.outbound.PaymentMethodCommandPort
import com.playground.payment.application.port.outbound.PaymentMethodQueryPort
import com.playground.payment.domain.exception.PaymentAccessDeniedException
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
        val billingKey =
            paymentGatewayPort.issueBillingKey(
                command.authKey,
                command.userId,
            )

        val isFirstCard = paymentMethodQueryPort.countByUserId(command.userId) == 0L
        val shouldBeDefault = command.setAsDefault || isFirstCard

        if (shouldBeDefault) {
            paymentMethodQueryPort.findDefaultOrNullByUserId(command.userId).ifPresent { existingDefault ->
                existingDefault.changeDefault(false)
                paymentMethodCommandPort.save(existingDefault)
            }
        }

        val newPaymentMethod =
            PaymentMethod(
                userId = command.userId,
                billingKey = billingKey,
                cardCompany = command.cardCompany,
                cardNumberMasked = command.cardNumberMasked,
                isDefault = shouldBeDefault,
            )

        return paymentMethodCommandPort.save(newPaymentMethod)
    }

    override fun getPaymentMethodList(userId: Long): List<PaymentMethod> = paymentMethodQueryPort.findAllByUserId(userId)

    override fun deletePaymentMethod(command: PaymentMethodDeleteCommand) {
        val paymentMethod = paymentMethodQueryPort.getById(command.paymentMethodId)

        if (paymentMethod.userId != command.userId) {
            throw PaymentAccessDeniedException(command.userId, command.paymentMethodId)
        }

        paymentMethodCommandPort.delete(paymentMethod)
    }
}
