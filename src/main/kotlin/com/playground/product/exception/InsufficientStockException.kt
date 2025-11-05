package com.playground.product.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class InsufficientStockException(message: String): BusinessException(ErrorCode.INSUFFICIENT_STOCK)