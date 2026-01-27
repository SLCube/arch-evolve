package com.playground.auth.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class InvalidRefreshTokenException :
    BusinessException(
        errorCode = ErrorCode.INVALID_REFRESH_TOKEN,
        message = ErrorCode.INVALID_REFRESH_TOKEN.message(),
    )
