package com.playground.payment.application.port.outbound

import com.playground.payment.domain.outbox.PaymentEventOutbox

interface OutboxCommandPort {
    fun save(outbox: PaymentEventOutbox): PaymentEventOutbox

    fun bulkMarkAsPublished(ids: List<Long>)
}
