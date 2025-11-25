package com.playground.payment.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class PaymentGatewayTimeout : BusinessException(
    errorCode = ErrorCode.PAYMENT_GATEWAY_TIMEOUT,
    message = ErrorCode.PAYMENT_GATEWAY_TIMEOUT.message()
)
