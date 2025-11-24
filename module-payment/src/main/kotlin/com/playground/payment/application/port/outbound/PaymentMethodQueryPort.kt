package com.playground.payment.application.port.outbound

import com.playground.payment.domain.model.PaymentMethod

fun interface PaymentMethodQueryPort {
    fun findByUserId(userId: Long) : PaymentMethod
}