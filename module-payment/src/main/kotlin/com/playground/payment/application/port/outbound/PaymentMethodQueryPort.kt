package com.playground.payment.application.port.outbound

import com.playground.payment.domain.model.PaymentMethod

interface PaymentMethodQueryPort {
    fun findByUserId(userId: Long) : PaymentMethod
    fun findAllByUserId(userId: Long) : List<PaymentMethod>
}