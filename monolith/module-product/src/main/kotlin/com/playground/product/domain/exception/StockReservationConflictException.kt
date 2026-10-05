package com.playground.product.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class StockReservationConflictException(
    orderId: Long,
) : BusinessException(
        errorCode = ErrorCode.STOCK_RESERVATION_CONFLICT,
        message = ErrorCode.STOCK_RESERVATION_CONFLICT.message(orderId),
    )
