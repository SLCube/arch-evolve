package com.playground.auth.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class RefreshTokenMismatchException :
    BusinessException(
        errorCode = ErrorCode.REFRESH_TOKEN_MISMATCH,
        message = ErrorCode.REFRESH_TOKEN_MISMATCH.message(),
    )
