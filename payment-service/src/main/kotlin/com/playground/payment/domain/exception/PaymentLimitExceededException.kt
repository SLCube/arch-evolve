package com.playground.payment.domain.exception

class PaymentLimitExceededException :
    BusinessException(
        errorCode = ErrorCode.PAYMENT_LIMIT_EXCEEDED,
        message = ErrorCode.PAYMENT_LIMIT_EXCEEDED.message(),
    )
