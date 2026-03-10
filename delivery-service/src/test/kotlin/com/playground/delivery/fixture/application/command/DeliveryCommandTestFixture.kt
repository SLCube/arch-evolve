package com.playground.delivery.fixture.application.command

import com.playground.delivery.application.port.inbound.command.DeliveryCreateCommand

object DeliveryCommandTestFixture {
    fun createCommand(
        orderId: Long = 100L,
        userId: Long = 2L,
        receiverName: String = "홍길동",
        receiverPhoneNumber: String = "010-1234-5678",
        zipCode: String = "12345",
        baseAddress: String = "서울시 강남구 테헤란로 123",
        detailAddress: String = "101호",
    ) = DeliveryCreateCommand(
        orderId = orderId,
        userId = userId,
        receiverName = receiverName,
        receiverPhoneNumber = receiverPhoneNumber,
        zipCode = zipCode,
        baseAddress = baseAddress,
        detailAddress = detailAddress,
    )
}
