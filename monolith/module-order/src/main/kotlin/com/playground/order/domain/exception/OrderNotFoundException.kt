package com.playground.order.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class OrderNotFoundException(
    orderId: Long,
) : BusinessException(
        errorCode = ErrorCode.ORDER_NOT_FOUND,
        message = ErrorCode.ORDER_NOT_FOUND.message(orderId),
    )
