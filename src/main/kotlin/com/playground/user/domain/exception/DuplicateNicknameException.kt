package com.playground.user.domain.exception

import com.playground.common.error.BusinessException
import com.playground.common.error.ErrorCode

class DuplicateNicknameException: BusinessException(
    ErrorCode.DUPLICATE_NICKNAME,
    ErrorCode.DUPLICATE_NICKNAME.message()
)