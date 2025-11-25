package com.playground.payment.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class PaymentLimitExceeded: BusinessException(
    errorCode = ErrorCode.PAYMENT_LIMIT_EXCEEDED,
    message = ErrorCode.PAYMENT_LIMIT_EXCEEDED.message()
)