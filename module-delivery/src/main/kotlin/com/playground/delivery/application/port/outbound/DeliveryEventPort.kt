package com.playground.delivery.application.port.outbound

import com.playground.common.event.DomainEvent

fun interface DeliveryEventPort {
    fun publish(event: DomainEvent)
}

