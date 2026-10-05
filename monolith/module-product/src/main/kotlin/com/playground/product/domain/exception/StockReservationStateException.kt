package com.playground.product.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class StockReservationStateException(orderId: Long) : BusinessException(
    errorCode = ErrorCode.STOCK_RESERVATION_STATE_INVALID,
    message = ErrorCode.STOCK_RESERVATION_STATE_INVALID.message(orderId),
)
