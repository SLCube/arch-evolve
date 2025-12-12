package com.playground.delivery.fixture.application.domain

import com.playground.delivery.domain.enum.DeliveryStatus
import com.playground.delivery.domain.model.Delivery
import com.playground.delivery.domain.model.DeliveryAddress
import com.playground.delivery.domain.model.DeliveryReceiver

object DeliveryDomainTestFixture {
    fun createDelivery(
        id: Long? = 1L,
        orderId: Long = 1L,
        userId: Long = 2L,
        receiverName: String = "홍길동",
        receiverPhoneNumber: String = "010-1234-5678",
        zipCode: String = "12345",
        baseAddress: String = "서울시 테스트구",
        detailAddress: String = "101동 202호",
        status: DeliveryStatus = DeliveryStatus.PENDING,
    ): Delivery = Delivery(
        id = id,
        orderId = orderId,
        userId = userId,
        deliveryReceiver = DeliveryReceiver(
            receiverName = receiverName,
            receiverPhoneNumber = receiverPhoneNumber,
        ),
        deliveryAddress = DeliveryAddress(
            zipCode = zipCode,
            baseAddress = baseAddress,
            detailAddress = detailAddress,
        ),
        deliveryStatus = status,
    )
}
