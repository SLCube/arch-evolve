package com.playground.payment.application.port.outbound

import com.playground.common.event.DomainEvent

fun interface PaymentEventPort {
    fun publish(event: DomainEvent)
}