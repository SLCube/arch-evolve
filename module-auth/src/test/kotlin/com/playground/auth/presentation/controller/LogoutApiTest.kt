package com.playground.auth.presentation.controller

import com.playground.auth.application.port.inbound.LogoutUseCase
import com.playground.auth.contract.security.AuthUserDetails
import com.playground.auth.presentation.annotation.AuthControllerSliceTest
import com.playground.support.RestDocsTest
import com.playground.support.docs.performAndDocument
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@AuthControllerSliceTest
class LogoutApiTest(
    @param:Autowired private val logoutUseCase: LogoutUseCase,
) : RestDocsTest() {

    @Test
    fun `로그아웃 - 성공`() {
        val userId = 1L
        val authUserDetails = AuthUserDetails(
            userId = userId,
            loginId = "testUser",
            password = "encodedPassword",
            roles = listOf("USER"),
        )

        val authentication = UsernamePasswordAuthenticationToken(
            authUserDetails,
            null,
            listOf(SimpleGrantedAuthority("ROLE_USER")),
        )

        SecurityContextHolder.getContext().authentication = authentication

        performAndDocument("로그아웃 - 성공") {
            tag = "인증 API"
            summary = "로그아웃"
            description = "사용자 로그아웃 및 Refresh Token 무효화"

            httpMethod = HttpMethod.DELETE
            urlTemplate = "/auth/logout"
            expectedStatus = status().isNoContent
        }

        verify(logoutUseCase).logout(userId)
    }
}
