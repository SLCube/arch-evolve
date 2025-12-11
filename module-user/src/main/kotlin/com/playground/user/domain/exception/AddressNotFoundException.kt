package com.playground.user.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class AddressNotFoundException(
    userId: Long,
    addressId: Long,
) : BusinessException(
    errorCode = ErrorCode.ADDRESS_NOT_FOUND,
    message = ErrorCode.ADDRESS_NOT_FOUND.message(userId, addressId),
)
