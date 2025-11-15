package com.playground.auth.application.port.out

fun interface AuthInfoPort {
    fun getLoginIdById(userId: Long): String?
}
