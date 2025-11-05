package com.playground.user.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class InValidPasswordException: BusinessException(ErrorCode.INVALID_INPUT) {
}