package com.playground.delivery.application.converter

import com.playground.delivery.application.port.inbound.command.DeliveryCreateCommand
import com.playground.delivery.domain.model.DeliveryAddress
import com.playground.delivery.domain.model.DeliveryReceiver

fun DeliveryCreateCommand.toDeliveryReceiver(): DeliveryReceiver =
    DeliveryReceiver(
        receiverName = receiverName,
        receiverPhoneNumber = receiverPhoneNumber,
    )

fun DeliveryCreateCommand.toDeliveryAddress(): DeliveryAddress =
    DeliveryAddress(
        zipCode = zipCode,
        baseAddress = baseAddress,
        detailAddress = detailAddress,
    )
