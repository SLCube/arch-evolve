package com.playground.auth.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.playground.auth.application.port.inbound.TokenIssueUseCase
import com.playground.auth.presentation.response.AuthTokenResponseDto
import io.kotest.matchers.shouldBe
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.Authentication
import java.nio.charset.StandardCharsets

@Suppress("NonAsciiCharacters")
class LoginSuccessHandlerTest {

    private val tokenIssueUseCase: TokenIssueUseCase = mock()
    private val objectMapper = jacksonObjectMapper()
    private val loginSuccessHandler = LoginSuccessHandler(tokenIssueUseCase, objectMapper)

    @Test
    fun `로그인 성공 시 accessToken 과 refreshToken 을 포함한 JSON 을 반환한다`() {
        val authentication: Authentication = mock()
        val tokenResponse = AuthTokenResponseDto(
            accessToken = "mock-access-token",
            refreshToken = "mock-refresh-token"
        )

        given(tokenIssueUseCase.issueTokens(any()))
            .willReturn(tokenResponse)

        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        loginSuccessHandler.onAuthenticationSuccess(request, response, authentication)

        response.status shouldBe HttpServletResponse.SC_OK
        response.contentType shouldBe "${MediaType.APPLICATION_JSON_VALUE};charset=${StandardCharsets.UTF_8.name()}"
        response.characterEncoding shouldBe StandardCharsets.UTF_8.name()

        val bodyNode = objectMapper.readTree(response.contentAsString)
        bodyNode["accessToken"].asText() shouldBe "mock-access-token"
        bodyNode["refreshToken"].asText() shouldBe "mock-refresh-token"
    }
}
