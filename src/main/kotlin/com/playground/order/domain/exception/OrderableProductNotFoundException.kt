package com.playground.order.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

data class OrderableProductNotFoundException(
    val productId: Long
): BusinessException(
    errorCode = ErrorCode.ORDERABLE_PRODUCT_NOT_FOUND,
    message =  ErrorCode.ORDERABLE_PRODUCT_NOT_FOUND.message(productId)
)
