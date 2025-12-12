package com.playground.user.fixture.application.command

import com.playground.user.application.port.inbound.command.AddressDefaultSetCommand
import com.playground.user.application.port.inbound.command.AddressRegisterCommand

object UserAddressCommandTestFixture {
    fun registerCommand(
        userId: Long = 1L,
        receiverName: String = "홍길동",
        receiverPhoneNumber: String = "01012345678",
        zipCode: String = "12345",
        baseAddress: String = "서울특별시 중구 세종대로",
        detailAddress: String = "101동 1001호",
    ) = AddressRegisterCommand(
        userId = userId,
        receiverName = receiverName,
        receiverPhoneNumber = receiverPhoneNumber,
        zipCode = zipCode,
        baseAddress = baseAddress,
        detailAddress = detailAddress,
    )

    fun defaultSetCommand(
        userId: Long = 1L,
        addressId: Long = 1L,
    ) = AddressDefaultSetCommand(
        userId = userId,
        addressId = addressId,
    )
}
