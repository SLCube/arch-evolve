package com.playground.user.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class UserNotFoundException: BusinessException(ErrorCode.USER_NOT_FOUND) {
}