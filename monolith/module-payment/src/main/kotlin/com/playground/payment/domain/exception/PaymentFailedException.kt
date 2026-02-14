package com.playground.payment.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class PaymentFailedException(
    reason: String?,
) : BusinessException(
    errorCode = ErrorCode.PAYMENT_GATEWAY_ERROR,
    message = reason ?: ErrorCode.PAYMENT_GATEWAY_ERROR.message()
)