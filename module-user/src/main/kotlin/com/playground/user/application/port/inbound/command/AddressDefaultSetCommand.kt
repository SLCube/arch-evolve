package com.playground.user.application.port.inbound.command

data class AddressDefaultSetCommand(
    val userId: Long,
    val addressId: Long,
)
