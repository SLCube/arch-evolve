package com.playground.auth.domain.model

data class AuthUser(
    val userId: Long,
    val loginId: String,
    val password: String,
) {
    override fun toString(): String = "AuthUser(userId=$userId, loginId='$loginId', password='****')" // toString 변경
}
