package com.playground.user.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class PasswordMismatchException : BusinessException(
    errorCode = ErrorCode.PASSWORD_MISMATCH,
    message = ErrorCode.PASSWORD_MISMATCH.message()
)
