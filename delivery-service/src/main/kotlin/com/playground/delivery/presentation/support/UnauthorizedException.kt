package com.playground.delivery.presentation.support

import com.playground.delivery.domain.exception.BusinessException
import com.playground.delivery.domain.exception.ErrorCode

class UnauthorizedException :
    BusinessException(
        errorCode = ErrorCode.UNAUTHORIZED,
        message = ErrorCode.UNAUTHORIZED.message(),
    )
