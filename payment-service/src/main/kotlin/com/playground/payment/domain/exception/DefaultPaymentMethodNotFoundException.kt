package com.playground.payment.domain.exception

class DefaultPaymentMethodNotFoundException(
    userId: Long,
) : BusinessException(
    errorCode = ErrorCode.DEFAULT_PAYMENT_METHOD_NOT_FOUND,
    message = ErrorCode.DEFAULT_PAYMENT_METHOD_NOT_FOUND.message(userId),
)