package com.playground.user.application.port.outbound

import com.playground.common.event.DomainEvent

fun interface UserEventPort {
    fun publish(event: DomainEvent)
}
