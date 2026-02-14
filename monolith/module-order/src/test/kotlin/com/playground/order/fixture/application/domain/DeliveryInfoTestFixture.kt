package com.playground.order.fixture.application.domain

import com.playground.delivery.contract.domain.vo.DeliveryInfo

object DeliveryInfoTestFixture {
    fun mockDeliveryInfo(
        deliveryId: Long = 100L,
        orderId: Long = 1L,
        userId: Long = 2L,
        receiverName: String = "홍길동",
        receiverPhoneNumber: String = "010-1111-2222",
        zipCode: String = "12345",
        baseAddress: String = "서울시 테스트구",
        detailAddress: String = "101동 202호",
        deliveryStatus: String = "PENDING",
    ): DeliveryInfo =
        DeliveryInfo(
            deliveryId = deliveryId,
            orderId = orderId,
            userId = userId,
            receiverName = receiverName,
            receiverPhoneNumber = receiverPhoneNumber,
            zipCode = zipCode,
            baseAddress = baseAddress,
            detailAddress = detailAddress,
            deliveryStatus = deliveryStatus,
        )
}

