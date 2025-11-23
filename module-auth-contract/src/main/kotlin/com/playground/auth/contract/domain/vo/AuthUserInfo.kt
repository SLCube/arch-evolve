package com.playground.auth.contract.domain.vo

data class AuthUserInfo(
    val userId: Long,
    val loginId: String,
    val password: String,
    val role: String,
)
