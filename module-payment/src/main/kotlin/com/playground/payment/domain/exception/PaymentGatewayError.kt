package com.playground.payment.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class PaymentGatewayError : BusinessException(
    errorCode = ErrorCode.PAYMENT_GATEWAY_ERROR,
    message = ErrorCode.PAYMENT_GATEWAY_ERROR.message()
)