package com.playground.payment.presentation.support

import com.playground.payment.domain.exception.BusinessException
import com.playground.payment.domain.exception.ErrorCode

class UnauthorizedException :
    BusinessException(
        errorCode = ErrorCode.UNAUTHORIZED,
        message = ErrorCode.UNAUTHORIZED.message(),
    )
