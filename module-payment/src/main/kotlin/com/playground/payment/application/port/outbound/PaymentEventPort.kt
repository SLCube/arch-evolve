package com.playground.payment.application.port.outbound

import com.playground.payment.consumer.PaymentEventConsumer

fun interface PaymentEventPort {
    fun publish(event: PaymentEventConsumer)
}