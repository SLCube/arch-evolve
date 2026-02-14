package com.playground.product.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class ProductNotFoundException(
    productId: Long,
) : BusinessException(
        errorCode = ErrorCode.PRODUCT_NOT_FOUND,
        message = ErrorCode.PRODUCT_NOT_FOUND.message(productId),
    )
