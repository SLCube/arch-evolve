package com.playground.common.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: String,
    val httpStatus: HttpStatus,
    private val message: String,
) {
    INVALID_INPUT("INVALID_INPUT", HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),

    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND, "id: %s, 상품을 찾을 수 없습니다."),
    INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", HttpStatus.BAD_REQUEST, "재고가 부족합니다. (상품 ID: %s, 현재 재고 : %s, 요청 수량: %s)"),

    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD("INVALID_PASSWORD", HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "인증이 필요한 요청입니다."),
    ;


    fun message(): String {
        return message
    }

    fun message(vararg args: Any?): String {
        return message.format(*args)
    }
}