package com.playground.user.infra.event.adapter

import com.playground.common.event.DomainEvent
import com.playground.user.application.port.out.UserEventPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class UserEventAdapter(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : UserEventPort {
    override fun publish(event: DomainEvent) {
        applicationEventPublisher.publishEvent(event)
    }
}
