package com.playground.user.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class InValidPasswordException: BusinessException(
    errorCode = ErrorCode.INVALID_INPUT,
    message = ErrorCode.INVALID_INPUT.message()
)