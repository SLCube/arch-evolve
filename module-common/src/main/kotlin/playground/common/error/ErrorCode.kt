package com.playground.common.error

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

    // User
    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    DUPLICATE_LOGIN_ID("DUPLICATE_LOGIN_ID", HttpStatus.CONFLICT, "이미 존재하는 로그인 ID입니다. (ID: %s)"),
    SAME_NICKNAME("SAME_NICKNAME", HttpStatus.BAD_REQUEST, "동일한 닉네임이 존재합니다."),
    DUPLICATE_NICKNAME("DUPLICATE_NICKNAME", HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    PASSWORD_MISMATCH("PASSWORD_MISMATCH", HttpStatus.BAD_REQUEST, "기존 비밀번호가 일치하지 않습니다."),
    BAD_CREDENTIALS("BAD_CREDENTIALS", HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),

    // Address
    ADDRESS_LIMIT_EXCEEDED("ADDRESS_LIMIT_EXCEEDED", HttpStatus.BAD_REQUEST, "주소 등록은 최대 %d개까지 가능합니다."),

    // Product
    PRODUCT_NOT_FOUND("PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다. (ID: %d)"),
    INSUFFICIENT_STOCK("INSUFFICIENT_STOCK", HttpStatus.BAD_REQUEST, "재고가 부족합니다. (상품 ID: %d, 현재 재고: %d, 요청 수량: %d)"),

    // Order
    ORDERABLE_PRODUCT_NOT_FOUND("ORDERABLE_PRODUCT_NOT_FOUND", HttpStatus.NOT_FOUND, "주문하려는 상품을 찾을 수 없습니다. (상품 ID: %d)"),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다. (ID: %d)"),
    ORDER_ACCESS_DENIED("ORDER_ACCESS_DENIED", HttpStatus.FORBIDDEN, "해당 주문에 대한 접근 권한이 없습니다. (주문 ID: %d, 사용자 ID: %d)"),
    ORDER_STATUS_INVALID_FOR_CANCEL(
        "ORDER_STATUS_INVALID_FOR_CANCEL",
        HttpStatus.BAD_REQUEST,
        "주문 상태가 PENDING 또는 COMPLETED일 때만 취소할 수 있습니다. (현재 상태: %s)",
    ),

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
