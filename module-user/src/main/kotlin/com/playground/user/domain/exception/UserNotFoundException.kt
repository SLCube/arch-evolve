package com.playground.user.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class UserNotFoundException :
    BusinessException(
        errorCode = ErrorCode.USER_NOT_FOUND,
        message = ErrorCode.USER_NOT_FOUND.message(),
    )
