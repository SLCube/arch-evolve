package com.playground.user.application.port.inbound.command

data class AddressRegisterCommand(
    val userId: Long,
    val receiverName: String,
    val receiverPhoneNumber: String,
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
)
