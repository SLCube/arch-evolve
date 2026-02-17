package com.playground.payment.domain.exception

class PaymentGatewayTimeoutException : BusinessException(
    errorCode = ErrorCode.PAYMENT_GATEWAY_TIMEOUT,
    message = ErrorCode.PAYMENT_GATEWAY_TIMEOUT.message()
)
