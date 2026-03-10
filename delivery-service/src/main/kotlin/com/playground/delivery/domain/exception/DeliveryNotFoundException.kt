package com.playground.delivery.domain.exception

class DeliveryNotFoundException(
    orderId: Long,
) : BusinessException(
        errorCode = ErrorCode.DELIVERY_NOT_FOUND,
        message = ErrorCode.DELIVERY_NOT_FOUND.message(orderId),
    )
