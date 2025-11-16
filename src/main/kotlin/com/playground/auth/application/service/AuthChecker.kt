package com.playground.auth.application.service

import com.playground.auth.application.port.outbound.AuthInfoPort
import org.springframework.stereotype.Component

@Component
class AuthChecker(
    private val authInfoPort: AuthInfoPort,
) {
    fun isOwner(
        username: String,
        userId: Long,
    ): Boolean {
        val loginId = authInfoPort.getLoginIdById(userId) ?: return false
        return loginId == username
    }
}
