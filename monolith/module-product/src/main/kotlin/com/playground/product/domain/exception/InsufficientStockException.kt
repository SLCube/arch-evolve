package com.playground.product.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class InsufficientStockException(
    productId: Long,
    requestQuantity: Int,
) : BusinessException(
        errorCode = ErrorCode.INSUFFICIENT_STOCK,
        message = ErrorCode.INSUFFICIENT_STOCK.message(productId, requestQuantity),
    )
