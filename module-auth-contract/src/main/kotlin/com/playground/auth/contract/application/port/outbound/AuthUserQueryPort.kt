package com.playground.auth.contract.application.port.outbound

import com.playground.auth.contract.domain.vo.AuthUserInfo

fun interface AuthUserQueryPort {
    fun getUserInfoByLoginId(loginId: String) : AuthUserInfo
}