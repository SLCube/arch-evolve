package com.playground.delivery.presentation.web.response

import com.playground.delivery.domain.model.Delivery
import java.time.LocalDateTime

data class DeliveryResponse(
    val deliveryId: Long,
    val orderId: Long,
    val receiverName: String,
    val receiverPhoneNumber: String,
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
    val deliveryStatus: String,
    val createdAt: LocalDateTime,
    val shippedAt: LocalDateTime?,
    val deliveredAt: LocalDateTime?,
) {
    companion object {
        fun from(delivery: Delivery): DeliveryResponse =
            DeliveryResponse(
                deliveryId = delivery.id!!,
                orderId = delivery.orderId,
                receiverName = delivery.deliveryReceiver.receiverName,
                receiverPhoneNumber = delivery.deliveryReceiver.receiverPhoneNumber,
                zipCode = delivery.deliveryAddress.zipCode,
                baseAddress = delivery.deliveryAddress.baseAddress,
                detailAddress = delivery.deliveryAddress.detailAddress,
                deliveryStatus = delivery.deliveryStatus.name,
                createdAt = delivery.createdAt,
                shippedAt = delivery.shippedAt,
                deliveredAt = delivery.deliveredAt,
            )
    }
}
