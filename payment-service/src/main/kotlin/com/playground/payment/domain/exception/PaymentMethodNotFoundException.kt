package com.playground.payment.domain.exception

class PaymentMethodNotFoundException(
    paymentMethodId: Long,
) : BusinessException(
        errorCode = ErrorCode.PAYMENT_METHOD_NOT_FOUND,
        message = ErrorCode.PAYMENT_METHOD_NOT_FOUND.message(paymentMethodId),
    )
