package com.playground.payment.application.port.outbound

import com.playground.payment.domain.outbox.PaymentEventOutbox

fun interface OutboxEventPublisherPort {
    fun publish(outbox: PaymentEventOutbox)
}
