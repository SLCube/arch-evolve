package com.playground.auth.contract.application.port.outbound

interface PasswordEncoderPort {
    fun encode(rawPassword: String): String
    fun matches(rawPassword: String, encodedPassword: String): Boolean
}