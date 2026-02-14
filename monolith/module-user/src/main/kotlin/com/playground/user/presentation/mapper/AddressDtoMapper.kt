package com.playground.user.presentation.mapper

import com.playground.user.application.port.inbound.command.AddressDefaultSetCommand
import com.playground.user.application.port.inbound.command.AddressRegisterCommand
import com.playground.user.presentation.request.AddressRegisterRequestDto

fun AddressRegisterRequestDto.toCommand(userId: Long): AddressRegisterCommand =
    AddressRegisterCommand(
        userId = userId,
        receiverName = this.receiverName,
        receiverPhoneNumber = this.receiverPhoneNumber,
        zipCode = this.zipCode,
        baseAddress = this.baseAddress,
        detailAddress = this.detailAddress,
    )

fun toAddressDefaultSetCommand(userId: Long, addressId: Long): AddressDefaultSetCommand =
    AddressDefaultSetCommand(
        userId = userId,
        addressId = addressId,
    )
