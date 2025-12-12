package com.playground.delivery.application.port.inbound

import com.playground.delivery.application.port.inbound.command.DeliveryCreateCommand
import com.playground.delivery.domain.model.Delivery

fun interface DeliveryUsecase {
    fun createDelivery(command: DeliveryCreateCommand): Delivery
}