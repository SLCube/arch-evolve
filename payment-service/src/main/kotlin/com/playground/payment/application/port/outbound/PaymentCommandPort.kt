package com.playground.payment.application.port.outbound

import com.playground.payment.domain.model.Payment

fun interface PaymentCommandPort {
    fun save(payment: Payment): Payment
}
