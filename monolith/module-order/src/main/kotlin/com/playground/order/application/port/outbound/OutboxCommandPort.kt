package com.playground.order.application.port.outbound

import com.playground.order.domain.outbox.OrderEventOutbox

interface OutboxCommandPort {
    fun save(outbox: OrderEventOutbox): OrderEventOutbox

    fun bulkMarkAsPublished(ids: List<Long>)

    fun bulkIncrementRetryCount(ids: List<Long>)

    fun bulkMarkAsFailed(ids: List<Long>)
}
