package com.playground.user.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class DuplicateLoginIdException(loginId: String): BusinessException(
    errorCode = ErrorCode.DUPLICATE_LOGIN_ID,
    message = ErrorCode.DUPLICATE_LOGIN_ID.message(loginId)
)