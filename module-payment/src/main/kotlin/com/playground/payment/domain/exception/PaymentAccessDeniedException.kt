package com.playground.payment.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class PaymentAccessDeniedException(
    userId: Long,
    paymentMethodId: Long,
) : BusinessException(
    errorCode = ErrorCode.PAYMENT_ACCESS_DENIED,
    message = ErrorCode.PAYMENT_ACCESS_DENIED.message(userId, paymentMethodId),
) {
}