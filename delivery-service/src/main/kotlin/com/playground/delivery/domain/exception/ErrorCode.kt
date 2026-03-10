package com.playground.delivery.domain.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: String,
    val httpStatus: HttpStatus,
    private val messageFormat: String,
) {
    INVALID_INPUT("INVALID_INPUT", HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),
    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    DELIVERY_NOT_FOUND("DELIVERY_NOT_FOUND", HttpStatus.NOT_FOUND, "배송 정보를 찾을 수 없습니다. (orderId: %d)"),
    DELIVERY_STATUS_INVALID(
        "DELIVERY_STATUS_INVALID",
        HttpStatus.BAD_REQUEST,
        "배송 상태 변경이 불가합니다. (deliveryId: %d, 현재 상태: %s, 변경 상태: %s)",
    ),
    ;

    fun message(vararg args: Any?): String = String.format(messageFormat, *args)
}
