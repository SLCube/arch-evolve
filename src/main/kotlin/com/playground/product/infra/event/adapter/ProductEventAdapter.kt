package com.playground.product.infra.event.adapter

import com.playground.common.event.DomainEvent
import com.playground.product.application.port.out.ProductEventPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class ProductEventAdapter(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : ProductEventPort {
    override fun publish(event: DomainEvent) {
        applicationEventPublisher.publishEvent(event)
    }
}
