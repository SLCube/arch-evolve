package com.playground.common.error

abstract class BusinessException(
    val errorCode: ErrorCode
): RuntimeException(errorCode.message())