package com.playground.delivery.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class DeliveryNotFoundException(
    orderId: Long,
) : BusinessException(
        errorCode = ErrorCode.DELIVERY_NOT_FOUND,
        message = ErrorCode.DELIVERY_NOT_FOUND.message(orderId),
    )

