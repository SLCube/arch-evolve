package com.playground.payment.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class PaymentLimitExceededException: BusinessException(
    errorCode = ErrorCode.PAYMENT_LIMIT_EXCEEDED,
    message = ErrorCode.PAYMENT_LIMIT_EXCEEDED.message()
)