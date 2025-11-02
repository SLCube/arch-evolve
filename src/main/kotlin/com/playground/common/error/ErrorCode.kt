package com.playground.common.error

enum class ErrorCode(
    val code: String,
    private val message: String,
) {
    INVALID_INPUT("INVALID_INPUT", "입력값이 유효하지 않습니다."),

    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", "id: %s, 상품을 찾을 수 없습니다.");

    fun message(): String {
        return message
    }

    fun message(vararg args: Any?): String {
        return message.format(*args)
    }
}