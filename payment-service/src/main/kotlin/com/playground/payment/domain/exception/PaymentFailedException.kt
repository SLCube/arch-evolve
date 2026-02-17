package com.playground.payment.domain.exception

class PaymentFailedException(
    reason: String?,
) : BusinessException(
    errorCode = ErrorCode.PAYMENT_GATEWAY_ERROR,
    message = reason ?: ErrorCode.PAYMENT_GATEWAY_ERROR.message()
)
