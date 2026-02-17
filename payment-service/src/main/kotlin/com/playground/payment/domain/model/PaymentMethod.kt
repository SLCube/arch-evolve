package com.playground.payment.domain.model

import java.time.LocalDateTime

class PaymentMethod(
    val id: Long? = null,
    val userId: Long,
    val billingKey: String,
    val cardCompany: String,
    val cardNumberMasked: String,
    var isDefault: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    fun changeDefault(isDefault: Boolean) {
        this.isDefault = isDefault
        updatedAt = LocalDateTime.now()
    }
}
