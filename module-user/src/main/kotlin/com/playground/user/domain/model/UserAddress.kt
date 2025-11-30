package com.playground.user.domain.model

data class UserAddress(
    val id: Long? = null,
    val userId: Long,
    val receiverName: String,
    val receiverPhoneNumber: String,
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
    var isDefault: Boolean = false,
) {
    fun unsetDefault() {
        this.isDefault = false
    }

    fun setDefault() {
        this.isDefault = true
    }
}
