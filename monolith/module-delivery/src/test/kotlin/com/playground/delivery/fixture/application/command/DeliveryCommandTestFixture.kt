package com.playground.delivery.fixture.application.command

import com.playground.delivery.application.port.inbound.command.DeliveryCreateCommand

object DeliveryCommandTestFixture {
    fun createDeliveryCommand(
        orderId: Long = 1L,
        userId: Long = 2L,
        receiverName: String = "홍길동",
        receiverPhoneNumber: String = "010-1234-5678",
        zipCode: String = "12345",
        baseAddress: String = "서울시 테스트구",
        detailAddress: String = "101동 202호",
    ): DeliveryCreateCommand = DeliveryCreateCommand(
        orderId = orderId,
        userId = userId,
        receiverName = receiverName,
        receiverPhoneNumber = receiverPhoneNumber,
        zipCode = zipCode,
        baseAddress = baseAddress,
        detailAddress = detailAddress,
    )
}
