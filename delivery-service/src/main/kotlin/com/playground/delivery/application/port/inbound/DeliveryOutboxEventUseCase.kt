package com.playground.delivery.application.port.inbound

interface DeliveryOutboxEventUseCase {
    fun pollAndPublishEvents()
}
