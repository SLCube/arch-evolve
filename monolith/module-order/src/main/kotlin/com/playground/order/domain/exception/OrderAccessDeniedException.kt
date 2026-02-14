package com.playground.order.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class OrderAccessDeniedException(
    orderId: Long,
    userId: Long,
) : BusinessException(
        errorCode = ErrorCode.ORDER_ACCESS_DENIED,
        message = ErrorCode.ORDER_ACCESS_DENIED.message(orderId, userId),
    )
