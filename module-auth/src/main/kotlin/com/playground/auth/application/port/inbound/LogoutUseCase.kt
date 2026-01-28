package com.playground.auth.application.port.inbound

interface LogoutUseCase {
    fun logout(userId: Long)
}
