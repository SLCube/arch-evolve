package com.playground.user.contract.domain.vo

data class ReceiverAddressInfo(
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
    val receiverName: String,
    val receiverPhoneNumber: String,
)
