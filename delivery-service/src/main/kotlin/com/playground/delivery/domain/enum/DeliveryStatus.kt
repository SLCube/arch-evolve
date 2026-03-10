package com.playground.delivery.domain.enum

enum class DeliveryStatus(
    val description: String,
) {
    PENDING("배송 준비중"),
    SHIPPING("배송 중"),
    DELIVERED("배송 완료"),
    FAILURE("배송 실패/취소"),
}
