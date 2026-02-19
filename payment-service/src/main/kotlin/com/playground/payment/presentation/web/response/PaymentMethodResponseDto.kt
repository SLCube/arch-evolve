package com.playground.payment.presentation.web.response

import com.playground.payment.domain.model.PaymentMethod
import java.time.LocalDateTime

data class PaymentMethodResponseDto(
    val id: Long,
    val cardCompany: String,
    val cardNumberMasked: String,
    val isDefault: Boolean,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun toResponse(domain: PaymentMethod): PaymentMethodResponseDto =
            PaymentMethodResponseDto(
                id = domain.id!!,
                cardCompany = domain.cardCompany,
                cardNumberMasked = domain.cardNumberMasked,
                isDefault = domain.isDefault,
                createdAt = domain.createdAt,
            )
    }
}
