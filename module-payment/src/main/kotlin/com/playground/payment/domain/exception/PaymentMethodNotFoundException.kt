package com.playground.payment.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class PaymentMethodNotFoundException(
    val userId: Long,
) : BusinessException(
    errorCode = ErrorCode.PAYMENT_METHOD_NOT_FOUND,
    message = ErrorCode.PAYMENT_METHOD_NOT_FOUND.message(userId),
)
