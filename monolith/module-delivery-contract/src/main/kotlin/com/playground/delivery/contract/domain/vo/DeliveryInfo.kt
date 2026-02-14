package com.playground.delivery.contract.domain.vo

data class DeliveryInfo(
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
