package com.playground.order.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode
import com.playground.order.domain.enum.OrderStatus

class OrderStatusInvalidException(
    currentStatus: OrderStatus,
) : BusinessException(
        errorCode = ErrorCode.ORDER_STATUS_INVALID_FOR_CANCEL,
        message = ErrorCode.ORDER_STATUS_INVALID_FOR_CANCEL.message(currentStatus.name),
    )
