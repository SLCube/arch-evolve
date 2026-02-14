package com.playground.auth.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.playground.common.error.ErrorCode
import io.kotest.matchers.shouldBe
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.access.AccessDeniedException
import java.nio.charset.StandardCharsets

@Suppress("NonAsciiCharacters")
class CustomAccessDeniedHandlerTest {

    private val objectMapper = jacksonObjectMapper()
    private val handler = CustomAccessDeniedHandler(objectMapper)

    @Test
    fun `권한이 없는 요청에 대해 403 JSON 응답을 반환한다`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val exception = AccessDeniedException("forbidden resource")

        handler.handle(request, response, exception)

        response.status shouldBe HttpServletResponse.SC_FORBIDDEN
        response.contentType shouldBe "${MediaType.APPLICATION_JSON_VALUE};charset=${StandardCharsets.UTF_8.name()}"
        response.characterEncoding shouldBe StandardCharsets.UTF_8.name()

        val bodyNode = objectMapper.readTree(response.contentAsString)
        bodyNode["code"].asText() shouldBe ErrorCode.FORBIDDEN.code
        bodyNode["message"].asText() shouldBe ErrorCode.FORBIDDEN.message()
    }
}
