package com.playground.auth.presentation.controller

import com.playground.auth.application.port.inbound.TokenRefreshUseCase
import com.playground.auth.domain.exception.InvalidRefreshTokenException
import com.playground.auth.presentation.annotation.AuthControllerSliceTest
import com.playground.auth.presentation.request.TokenRefreshRequestDto
import com.playground.auth.presentation.response.AuthTokenResponseDto
import com.playground.support.RestDocsTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@AuthControllerSliceTest
class TokenRefreshApiTest(
    @param:Autowired private val tokenRefreshUseCase: TokenRefreshUseCase,
) : RestDocsTest() {

    @Test
    fun `토큰 갱신 성공`() {
        val request = TokenRefreshRequestDto(refreshToken = "valid-refresh-token")
        val response = AuthTokenResponseDto(
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
        )

        given(tokenRefreshUseCase.refresh(any()))
            .willReturn(response)

        performAndDocument("토큰 갱신 성공") {
            tag = "인증 API"
            summary = "토큰 갱신"
            description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token 발급"

            httpMethod = HttpMethod.POST
            urlTemplate = "/auth/refresh"
            requestBody = request
            expectedStatus = status().isOk
            additionalMatchers = arrayOf(
                jsonPath("$.accessToken").value("new-access-token"),
                jsonPath("$.refreshToken").value("new-refresh-token"),
            )
            snippets = arrayOf(
                requestFields(
                    fieldWithPath("refreshToken").description("리프레시 토큰"),
                ),
                responseFields(
                    fieldWithPath("accessToken").description("새로운 액세스 토큰"),
                    fieldWithPath("refreshToken").description("새로운 리프레시 토큰"),
                ),
            )
        }
    }

    @Test
    fun `토큰 갱신 실패 - 만료된 토큰`() {
        val request = TokenRefreshRequestDto(refreshToken = "expired-refresh-token")

        given(tokenRefreshUseCase.refresh(any()))
            .willThrow(InvalidRefreshTokenException())

        performAndDocument("토큰 갱신 실패 - 만료된 토큰") {
            tag = "인증 API"
            summary = "토큰 갱신"
            description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token 발급"

            httpMethod = HttpMethod.POST
            urlTemplate = "/auth/refresh"
            requestBody = request
            expectedStatus = status().isUnauthorized
            additionalMatchers = arrayOf(
                jsonPath("$.code").exists(),
                jsonPath("$.message").exists(),
            )
            snippets = arrayOf(
                requestFields(
                    fieldWithPath("refreshToken").description("리프레시 토큰"),
                ),
                responseFields(commonErrorResponseSnippet()),
            )
        }
    }
}
