package com.playground.delivery.domain.exception

class ForbiddenException :
    BusinessException(
        errorCode = ErrorCode.FORBIDDEN,
        message = ErrorCode.FORBIDDEN.message(),
    )
