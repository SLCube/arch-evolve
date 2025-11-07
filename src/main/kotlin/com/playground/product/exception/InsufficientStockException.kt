package com.playground.product.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class InsufficientStockException(
    productId: Long,
    currentStock: Int,
    requestQuantity: Int
): BusinessException(
    errorCode = ErrorCode.INSUFFICIENT_STOCK,
    message = ErrorCode.INSUFFICIENT_STOCK.message(productId, currentStock, requestQuantity)
)