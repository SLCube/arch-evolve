package com.playground.auth.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.playground.common.error.ErrorCode
import io.kotest.matchers.shouldBe
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.nio.charset.StandardCharsets

@Suppress("NonAsciiCharacters")
class LoginFailureHandlerTest {

    private val objectMapper = jacksonObjectMapper()
    private val loginFailureHandler = LoginFailureHandler(objectMapper)

    @Test
    fun `아이디 또는 비밀번호 오류 시 BAD_CREDENTIALS 응답을 반환한다`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val exception: AuthenticationException = BadCredentialsException("invalid")

        loginFailureHandler.onAuthenticationFailure(request, response, exception)

        assertUnauthorizedResponse(response, ErrorCode.BAD_CREDENTIALS)
    }

    @Test
    fun `기타 인증 예외 시 UNAUTHORIZED 응답을 반환한다`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val exception: AuthenticationException = AuthenticationServiceException("error")

        loginFailureHandler.onAuthenticationFailure(request, response, exception)

        assertUnauthorizedResponse(response, ErrorCode.UNAUTHORIZED)
    }

    @Test
    fun `사용자를 찾지 못한 경우에도 BAD_CREDENTIALS 응답을 반환한다`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val exception: AuthenticationException = UsernameNotFoundException("missing")

        loginFailureHandler.onAuthenticationFailure(request, response, exception)

        assertUnauthorizedResponse(response, ErrorCode.BAD_CREDENTIALS)
    }

    private fun assertUnauthorizedResponse(
        response: MockHttpServletResponse,
        expectedErrorCode: ErrorCode,
    ) {
        response.status shouldBe HttpServletResponse.SC_UNAUTHORIZED
        response.contentType shouldBe "${MediaType.APPLICATION_JSON_VALUE};charset=${StandardCharsets.UTF_8.name()}"
        response.characterEncoding shouldBe StandardCharsets.UTF_8.name()

        val bodyNode = objectMapper.readTree(response.contentAsString)
        bodyNode["code"].asText() shouldBe expectedErrorCode.code
        bodyNode["message"].asText() shouldBe expectedErrorCode.message()
    }
}
