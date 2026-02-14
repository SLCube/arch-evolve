package com.playground.payment.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class DefaultPaymentMethodNotFoundException(
    userId: Long,
) : BusinessException(
    errorCode = ErrorCode.DEFAULT_PAYMENT_METHOD_NOT_FOUND,
    message = ErrorCode.DEFAULT_PAYMENT_METHOD_NOT_FOUND.message(userId),
) {
}