package com.playground.payment.application.port.outbound

import com.playground.payment.domain.outbox.PaymentEventOutbox

interface PaymentEventPublisherPort {
    fun publishAll(outboxes: List<PaymentEventOutbox>): List<PaymentEventOutbox>
}
