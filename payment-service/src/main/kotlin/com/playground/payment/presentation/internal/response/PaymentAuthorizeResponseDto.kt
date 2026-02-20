package com.playground.payment.presentation.internal.response

import com.playground.payment.domain.enum.PaymentStatus
import com.playground.payment.domain.model.Payment

data class PaymentAuthorizeResponseDto(
    val paymentId: Long,
    val orderId: Long,
    val pgTransactionId: String,
    val approvalNumber: String,
    val status: PaymentStatus,
) {
    companion object {
        fun from(payment: Payment): PaymentAuthorizeResponseDto =
            PaymentAuthorizeResponseDto(
                paymentId = payment.id!!,
                orderId = payment.orderId,
                pgTransactionId = payment.pgTransactionId!!,
                approvalNumber = payment.approvalNumber!!,
                status = payment.status,
            )
    }
}
