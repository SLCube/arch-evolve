package com.playground.delivery.application.port.outbound

import com.playground.delivery.domain.outbox.DeliveryEventOutbox

interface OutboxCommandPort {
    fun save(outbox: DeliveryEventOutbox): DeliveryEventOutbox

    fun bulkMarkAsPublished(ids: List<Long>)
}
