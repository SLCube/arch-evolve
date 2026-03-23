package com.playground.order.application.port.inbound

interface OrderOutboxEventUseCase {
    fun pollAndPublishEvents()

    fun recordMetrics()
}
