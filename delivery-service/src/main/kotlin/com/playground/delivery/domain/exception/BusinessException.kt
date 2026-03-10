package com.playground.delivery.domain.exception

abstract class BusinessException(
    val errorCode: ErrorCode,
    override val message: String,
) : RuntimeException(message)
