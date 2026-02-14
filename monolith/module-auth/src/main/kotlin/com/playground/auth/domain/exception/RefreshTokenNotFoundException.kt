package com.playground.auth.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class RefreshTokenNotFoundException :
    BusinessException(
        errorCode = ErrorCode.REFRESH_TOKEN_NOT_FOUND,
        message = ErrorCode.REFRESH_TOKEN_NOT_FOUND.message(),
    )
