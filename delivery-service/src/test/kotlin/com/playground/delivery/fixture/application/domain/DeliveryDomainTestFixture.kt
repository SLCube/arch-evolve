package com.playground.delivery.fixture.application.domain

import com.playground.delivery.domain.enum.DeliveryStatus
import com.playground.delivery.domain.model.Delivery
import com.playground.delivery.domain.model.DeliveryAddress
import com.playground.delivery.domain.model.DeliveryReceiver
import java.time.LocalDateTime

object DeliveryDomainTestFixture {
    fun mockDelivery(
        id: Long? = 1L,
        orderId: Long = 100L,
        userId: Long = 2L,
        receiverName: String = "홍길동",
        receiverPhoneNumber: String = "010-1234-5678",
        zipCode: String = "12345",
        baseAddress: String = "서울시 강남구 테헤란로 123",
        detailAddress: String = "101호",
        deliveryStatus: DeliveryStatus = DeliveryStatus.PENDING,
        createdAt: LocalDateTime = LocalDateTime.of(2024, 1, 1, 0, 0),
        shippedAt: LocalDateTime? = null,
        deliveredAt: LocalDateTime? = null,
    ) = Delivery(
        id = id,
        orderId = orderId,
        userId = userId,
        deliveryReceiver =
            DeliveryReceiver(
                receiverName = receiverName,
                receiverPhoneNumber = receiverPhoneNumber,
            ),
        deliveryAddress =
            DeliveryAddress(
                zipCode = zipCode,
                baseAddress = baseAddress,
                detailAddress = detailAddress,
            ),
        deliveryStatus = deliveryStatus,
        createdAt = createdAt,
        shippedAt = shippedAt,
        deliveredAt = deliveredAt,
    )
}
