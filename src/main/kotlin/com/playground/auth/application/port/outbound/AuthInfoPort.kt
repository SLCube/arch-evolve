package com.playground.auth.application.port.outbound

fun interface AuthInfoPort {
    fun getLoginIdById(userId: Long): String?
}
