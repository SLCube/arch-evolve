package com.playground.payment.domain.enum

enum class PaymentStatus(
    val description: String,
) {
    PENDING("결제 대기"),
    COMPLETED("결제 완료"),
    FAILED("결제 실패"),
    CANCELLED("결제 취소"),
    ;

    fun isSuccess() : Boolean = this == COMPLETED
}