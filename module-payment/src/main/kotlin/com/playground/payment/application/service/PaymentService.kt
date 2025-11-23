package com.playground.payment.application.service

import com.playground.payment.application.port.inbound.PaymentUseCase
import com.playground.payment.application.port.inbound.command.AuthorizePaymentCommand
import com.playground.payment.domain.model.Payment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PaymentService(

) : PaymentUseCase {
    override fun authorizePayment(command: AuthorizePaymentCommand): Payment {
        TODO("Not yet implemented")
    }
}