package com.playground.order.infra.http.dto

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
)
