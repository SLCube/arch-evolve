package com.playground.user.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class DuplicateNicknameException: BusinessException(
    ErrorCode.DUPLICATE_NICKNAME,
    ErrorCode.DUPLICATE_NICKNAME.message()
)