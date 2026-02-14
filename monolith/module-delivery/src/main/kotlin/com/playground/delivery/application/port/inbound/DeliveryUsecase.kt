package com.playground.delivery.application.port.inbound

import com.playground.delivery.application.port.inbound.command.DeliveryCreateCommand
import com.playground.delivery.domain.model.Delivery

interface DeliveryUsecase {
    fun createDelivery(command: DeliveryCreateCommand): Delivery
    fun startDelivery(orderId: Long): Delivery
    fun completeDelivery(orderId: Long): Delivery
}
