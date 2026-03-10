package com.playground.delivery.application.port.outbound

import com.playground.delivery.domain.outbox.DeliveryEventOutbox

interface DeliveryEventPublisherPort {
    fun publishAll(outboxes: List<DeliveryEventOutbox>): List<DeliveryEventOutbox>
}
