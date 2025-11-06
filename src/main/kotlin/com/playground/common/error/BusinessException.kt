package com.playground.common.error

abstract class BusinessException(
    val errorCode: ErrorCode,
    override val message: String
): RuntimeException(message)