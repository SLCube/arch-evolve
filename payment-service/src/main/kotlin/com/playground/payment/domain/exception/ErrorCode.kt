package com.playground.payment.domain.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: String,
    val httpStatus: HttpStatus,
    private val messageFormat: String,
) {
    // Common
    INVALID_INPUT("INVALID_INPUT", HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),
    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "인증이 필요한 요청입니다."),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // Payment
    DEFAULT_PAYMENT_METHOD_NOT_FOUND("DEFAULT_PAYMENT_METHOD_NOT_FOUND", HttpStatus.NOT_FOUND, "사용자 ID : %d에 등록된 기본 결제수단이 없습니다."),
    PAYMENT_METHOD_NOT_FOUND("PAYMENT_METHOD_NOT_FOUND", HttpStatus.NOT_FOUND, "결제 수단을 찾을 수 없습니다. (결제 수단 ID: %d)"),
    PAYMENT_ACCESS_DENIED("PAYMENT_ACCESS_DENIED", HttpStatus.FORBIDDEN, "해당 결제 수단에 대한 접근 권한이 없습니다. (사용자 ID: %d, 결제 수단 ID: %d)"),
    PAYMENT_GATEWAY_TIMEOUT("PAYMENT_GATEWAY_TIMEOUT", HttpStatus.GATEWAY_TIMEOUT, "PG사 응답 지연(Timeout) 발생"),
    PAYMENT_LIMIT_EXCEEDED("PAYMENT_LIMIT_EXCEEDED", HttpStatus.BAD_REQUEST, "결제 한도 초과"),
    PAYMENT_GATEWAY_ERROR("PAYMENT_GATEWAY_ERROR", HttpStatus.BAD_GATEWAY, "PG사 오류 발생"),

    ;

    fun message(vararg args: Any?): String = String.format(messageFormat, *args)
}
