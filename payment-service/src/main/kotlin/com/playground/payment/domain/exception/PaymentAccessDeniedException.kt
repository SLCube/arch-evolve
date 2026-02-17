package com.playground.payment.domain.exception

class PaymentAccessDeniedException(
    userId: Long,
    paymentMethodId: Long,
) : BusinessException(
        errorCode = ErrorCode.PAYMENT_ACCESS_DENIED,
        message = ErrorCode.PAYMENT_ACCESS_DENIED.message(userId, paymentMethodId),
    )
