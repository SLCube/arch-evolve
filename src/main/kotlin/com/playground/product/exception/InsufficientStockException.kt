package com.playground.product.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class InsufficientStockException: BusinessException(
    errorCode = ErrorCode.INSUFFICIENT_STOCK,
    message = ErrorCode.INSUFFICIENT_STOCK.message()
)