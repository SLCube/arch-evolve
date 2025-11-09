package com.playground.user.domain

import com.playground.user.domain.enum.UserRole
import com.playground.user.domain.exception.SameNicknameException

class User(
    val id: Long? = null,
    val loginId: String,
    var password: String,
    var nickname: String,
    val role: UserRole = UserRole.USER
) {

    fun updateNickname(newNickname: String) {
        if (this.nickname == newNickname) {
            throw SameNicknameException()
        }
        this.nickname = newNickname
    }

    fun updatePassword(encodedNewPassword: String) {
        this.password = encodedNewPassword
    }
}