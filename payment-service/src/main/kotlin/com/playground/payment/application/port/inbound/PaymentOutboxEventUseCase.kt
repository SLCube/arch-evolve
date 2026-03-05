package com.playground.payment.application.port.inbound

interface PaymentOutboxEventUseCase {
    fun pollAndPublishEvents()
}
