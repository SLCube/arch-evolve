package com.playground.product.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class ProductNotFoundException(productId: Long) : BusinessException(ErrorCode.PRODUCT_NOT_FOUND) {
    override val message: String = errorCode.message(productId)
}