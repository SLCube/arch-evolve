package com.playground.payment.application.port.outbound

import com.playground.payment.domain.model.PaymentMethod

interface PaymentMethodCommandPort {
    fun save(paymentMethod: PaymentMethod): PaymentMethod

    fun delete(paymentMethod: PaymentMethod)
}
