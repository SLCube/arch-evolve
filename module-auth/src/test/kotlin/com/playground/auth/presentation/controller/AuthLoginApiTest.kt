package com.playground.auth.presentation.controller

import com.playground.auth.application.port.inbound.TokenIssueUseCase
import com.playground.auth.presentation.annotation.AuthControllerSliceTest
import com.playground.auth.presentation.request.AuthLoginRequestDto
import com.playground.auth.presentation.response.AuthTokenResponseDto
import com.playground.common.error.ErrorCode
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
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@AuthControllerSliceTest
class AuthLoginApiTest(
    @param:Autowired private val authenticationManager: AuthenticationManager,
    @param:Autowired private val tokenIssueUseCase: TokenIssueUseCase,
) : RestDocsTest() {

    @Test
    fun `로그인 - 성공`() {
        val loginRequest =
            AuthLoginRequestDto(
                loginId = "testUser",
                password = "password123",
            )

        val authenticatedToken =
            UsernamePasswordAuthenticationToken(
                loginRequest.loginId,
                null,
                listOf(SimpleGrantedAuthority("ROLE_USER")),
            )

        given(authenticationManager.authenticate(any()))
            .willReturn(authenticatedToken)

        given(tokenIssueUseCase.issueTokens(any()))
            .willReturn(
                AuthTokenResponseDto(
                    accessToken = "mock-access-token",
                    refreshToken = "mock-refresh-token",
                )
            )

        performAndDocument("로그인 - 성공") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/users/login"
            requestBody = loginRequest
            expectedStatus = status().isOk
            additionalMatchers =
                arrayOf(
                    jsonPath("$.accessToken").value("mock-access-token"),
                    jsonPath("$.refreshToken").value("mock-refresh-token"),
                )
            snippets =
                arrayOf(
                    requestFields(
                        fieldWithPath("loginId").description("로그인 ID"),
                        fieldWithPath("password").description("비밀번호"),
                    ),
                    responseFields(
                        fieldWithPath("accessToken").description("액세스 토큰"),
                        fieldWithPath("refreshToken").description("리프레시 토큰"),
                    ),
                )
        }
    }

    @Test
    fun `로그인 - 실패(잘못된 자격 증명)`() {
        val loginRequest =
            AuthLoginRequestDto(
                loginId = "unknownUser",
                password = "wrongPassword",
            )

        given(authenticationManager.authenticate(any()))
            .willAnswer { throw BadCredentialsException("invalid credentials") }

        performAndDocument("로그인 - 실패 (존재하지 않는 사용자)") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/users/login"
            requestBody = loginRequest
            expectedStatus = status().isUnauthorized
            additionalMatchers =
                arrayOf(
                    jsonPath("$.code").value(ErrorCode.BAD_CREDENTIALS.code),
                    jsonPath("$.message").value(ErrorCode.BAD_CREDENTIALS.message()),
                )
            snippets =
                arrayOf(
                    requestFields(
                        fieldWithPath("loginId").description("로그인 ID"),
                        fieldWithPath("password").description("비밀번호"),
                    ),
                    responseFields(commonErrorResponseSnippet()),
                )
        }
    }
}
