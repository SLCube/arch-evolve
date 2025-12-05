package com.playground.delivery.application.port.inbound

import com.playground.delivery.application.port.inbound.command.DeliveryCreateCommand

fun interface DeliveryUsecase {
    fun createDelivery(command: DeliveryCreateCommand)
}