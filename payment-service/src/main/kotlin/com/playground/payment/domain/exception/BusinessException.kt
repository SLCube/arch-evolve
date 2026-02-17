package com.playground.payment.domain.exception

abstract class BusinessException(
    val errorCode: ErrorCode,
    override val message: String,
) : RuntimeException(message)
