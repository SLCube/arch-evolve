package com.playground.user.application.port.out

import com.playground.common.event.DomainEvent

fun interface UserEventPort {
    fun publish(event: DomainEvent)
}