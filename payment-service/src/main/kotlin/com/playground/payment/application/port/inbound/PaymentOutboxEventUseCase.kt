package com.playground.payment.application.port.inbound

import com.playground.payment.domain.outbox.PaymentEventOutbox

interface PaymentOutboxEventUseCase {
    fun findPendingEvents(): List<PaymentEventOutbox>

    fun publishEvent(outbox: PaymentEventOutbox)
}
