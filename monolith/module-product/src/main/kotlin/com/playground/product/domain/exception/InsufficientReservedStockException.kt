package com.playground.product.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class InsufficientReservedStockException(
    productId: Long,
    requestQuantity: Int,
) : BusinessException(
        errorCode = ErrorCode.INSUFFICIENT_RESERVED_STOCK,
        message = ErrorCode.INSUFFICIENT_RESERVED_STOCK.message(productId, requestQuantity),
    )
