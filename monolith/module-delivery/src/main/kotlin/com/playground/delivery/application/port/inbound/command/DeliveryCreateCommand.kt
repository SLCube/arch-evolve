package com.playground.delivery.application.port.inbound.command

data class DeliveryCreateCommand(
    val orderId: Long,
    val userId: Long,
    val receiverName: String,
    val receiverPhoneNumber: String,
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
)
