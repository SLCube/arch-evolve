package com.playground.auth.infra.adapter

import com.playground.auth.contract.application.port.outbound.PasswordEncoderPort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptAdapter(
    private val passwordEncoder: PasswordEncoder,
) : PasswordEncoderPort {
    override fun encode(rawPassword: String): String {
        return passwordEncoder.encode(rawPassword)
    }

    override fun matches(rawPassword: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(rawPassword, encodedPassword)
    }
}