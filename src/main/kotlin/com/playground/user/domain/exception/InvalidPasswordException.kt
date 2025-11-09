package com.playground.user.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class InvalidPasswordException: BusinessException(
    errorCode = ErrorCode.INVALID_PASSWORD,
    message = ErrorCode.INVALID_PASSWORD.message()
)