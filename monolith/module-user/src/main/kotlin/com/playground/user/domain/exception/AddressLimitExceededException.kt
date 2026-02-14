package com.playground.user.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class AddressLimitExceededException(
    val limit: Int
) : BusinessException(
    errorCode = ErrorCode.ADDRESS_LIMIT_EXCEEDED,
    message = ErrorCode.ADDRESS_LIMIT_EXCEEDED.message(limit)
) {
}