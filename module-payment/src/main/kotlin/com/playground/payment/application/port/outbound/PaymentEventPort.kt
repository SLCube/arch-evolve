package com.playground.payment.application.port.outbound

import com.playground.payment.contract.domain.event.PaymentCompletedEvent

fun interface PaymentEventPort {
    fun publish(event: PaymentCompletedEvent)
}