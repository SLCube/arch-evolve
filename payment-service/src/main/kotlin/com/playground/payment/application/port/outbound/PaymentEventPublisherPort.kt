package com.playground.payment.application.port.outbound

import com.playground.payment.domain.outbox.PaymentEventOutbox

fun interface PaymentEventPublisherPort {
    fun publish(outbox: PaymentEventOutbox)
}
