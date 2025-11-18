package com.playground.user.controller

import com.playground.auth.presentation.request.AuthLoginRequestDto
import com.playground.support.ApiTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
class UserLoginApiTest : ApiTest() {
    @Test
    fun `로그인 - 성공`() {
        createUser("testUser", "password123", "테스트유저")

        val loginRequest =
            AuthLoginRequestDto(
                loginId = "testUser",
                password = "password123",
            )

        performAndDocument("로그인 - 성공") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/users/login"
            requestBody = loginRequest
            expectedStatus = status().isOk
            additionalMatchers =
                arrayOf(
                    jsonPath("$.accessToken").isNotEmpty,
                )
            snippets =
                arrayOf(
                    requestFields(
                        fieldWithPath("loginId").description("로그인 ID"),
                        fieldWithPath("password").description("비밀번호"),
                    ),
                    responseFields(
                        fieldWithPath("accessToken").description("인증 토큰"),
                    ),
                )
        }
    }

    @Test
    fun `로그인 - 실패 (존재하지 않는 사용자)`() {
        val loginRequest =
            AuthLoginRequestDto(
                loginId = "nonExistentUser",
                password = "password123",
            )

        performAndDocument("로그인 - 실패 (존재하지 않는 사용자)") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/users/login"
            requestBody = loginRequest
            expectedStatus = status().isUnauthorized
            additionalMatchers =
                arrayOf(
                    jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."),
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

    @Test
    fun `로그인 - 실패 (비밀번호 불일치)`() {
        createUser("testUser", "password123", "테스트유저")

        val loginRequest =
            AuthLoginRequestDto(
                loginId = "testUser",
                password = "wrongPassword",
            )

        performAndDocument("로그인 - 실패 (비밀번호 불일치)") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/users/login"
            requestBody = loginRequest
            expectedStatus = status().isUnauthorized
            additionalMatchers =
                arrayOf(
                    jsonPath("$.message").value("아이디 또는 비밀번호가 일치하지 않습니다."),
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
