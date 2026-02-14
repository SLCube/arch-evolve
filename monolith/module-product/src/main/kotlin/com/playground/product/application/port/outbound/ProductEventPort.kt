package com.playground.product.application.port.outbound

import com.playground.common.event.DomainEvent

fun interface ProductEventPort {
    fun publish(event: DomainEvent)
}
