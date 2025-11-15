package com.playground.common.error

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: String,
    val httpStatus: HttpStatus,
    private val messageFormat: String
) {
    // Common
    INVALID_INPUT("INVALID_INPUT", HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),
    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "인증이 필요한 요청입니다."),

    // User
    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATE_LOGIN_ID("DUPLICATE_LOGIN_ID", HttpStatus.CONFLICT, "이미 존재하는 로그인 ID입니다. (ID: %s)"),
    SAME_NICKNAME("SAME_NICKNAME", HttpStatus.BAD_REQUEST, "동일한 닉네임이 존재합니다."),
    DUPLICATE_NICKNAME("DUPLICATE_NICKNAME", HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    PASSWORD_MISMATCH("PASSWORD_MISMATCH", HttpStatus.BAD_REQUEST, "기존 비밀번호가 일치하지 않습니다."),
    INVALID_PASSWORD("INVALID_PASSWORD", HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),

    // Product
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다. (ID: %d)"),
    INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", HttpStatus.BAD_REQUEST, "재고가 부족합니다. (상품 ID: %d, 현재 재고: %d, 요청 수량: %d)"),

    // Order
    ORDERABLE_PRODUCT_NOT_FOUND("ORDERABLE_PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND, "주문하려는 상품을 찾을 수 없습니다. (상품 ID: %d)")
    ;

    fun message(vararg args: Any?): String {
        return String.format(messageFormat, *args)
    }
}