package com.playground.delivery.application.port.inbound

import com.playground.delivery.application.port.inbound.command.DeliveryCreateCommand
import com.playground.delivery.domain.model.Delivery

interface DeliveryUseCase {
    fun createDelivery(command: DeliveryCreateCommand): Delivery
}
