package com.playground.payment.application.port.outbound

import com.playground.payment.domain.model.Payment

fun interface PaymentQueryPort {
    fun findByOrderId(orderId: Long): Payment?
}