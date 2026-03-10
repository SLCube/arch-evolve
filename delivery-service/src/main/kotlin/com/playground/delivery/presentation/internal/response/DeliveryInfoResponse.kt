package com.playground.delivery.presentation.internal.response

import com.playground.delivery.domain.model.Delivery

data class DeliveryInfoResponse(
    val deliveryId: Long,
    val orderId: Long,
    val userId: Long,
    val receiverName: String,
    val receiverPhoneNumber: String,
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
    val deliveryStatus: String,
) {
    companion object {
        fun from(delivery: Delivery): DeliveryInfoResponse =
            DeliveryInfoResponse(
                deliveryId = delivery.id!!,
                orderId = delivery.orderId,
                userId = delivery.userId,
                receiverName = delivery.deliveryReceiver.receiverName,
                receiverPhoneNumber = delivery.deliveryReceiver.receiverPhoneNumber,
                zipCode = delivery.deliveryAddress.zipCode,
                baseAddress = delivery.deliveryAddress.baseAddress,
                detailAddress = delivery.deliveryAddress.detailAddress,
                deliveryStatus = delivery.deliveryStatus.name,
            )
    }
}
