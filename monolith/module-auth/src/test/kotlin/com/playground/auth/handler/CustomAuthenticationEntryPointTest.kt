package com.playground.auth.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.playground.common.error.ErrorCode
import io.kotest.matchers.shouldBe
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.AuthenticationException
import java.nio.charset.StandardCharsets

@Suppress("NonAsciiCharacters")
class CustomAuthenticationEntryPointTest {

    private val objectMapper = jacksonObjectMapper()
    private val entryPoint = CustomAuthenticationEntryPoint(objectMapper)

    @Test
    fun `인증되지 않은 요청에 대해 401 JSON 응답을 반환한다`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val exception = object : AuthenticationException("unauthorized") {}

        entryPoint.commence(request, response, exception)

        response.status shouldBe HttpServletResponse.SC_UNAUTHORIZED
        response.contentType shouldBe "${MediaType.APPLICATION_JSON_VALUE};charset=${StandardCharsets.UTF_8.name()}"
        response.characterEncoding shouldBe StandardCharsets.UTF_8.name()

        val bodyNode = objectMapper.readTree(response.contentAsString)
        bodyNode["code"].asText() shouldBe ErrorCode.UNAUTHORIZED.code
        bodyNode["message"].asText() shouldBe ErrorCode.UNAUTHORIZED.message()
    }
}
