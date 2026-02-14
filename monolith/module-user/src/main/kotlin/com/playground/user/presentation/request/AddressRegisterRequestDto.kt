package com.playground.user.presentation.request

data class AddressRegisterRequestDto(
    val receiverName: String,
    val receiverPhoneNumber: String,
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
)
