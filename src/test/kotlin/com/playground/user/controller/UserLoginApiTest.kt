package com.playground.user.controller

import com.playground.support.ApiTest
import com.playground.support.docs.performAndDocument
import com.playground.user.presentation.request.UserLoginRequestDto
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
class UserLoginApiTest: ApiTest() {

    @Test
    fun `로그인 - 성공`() {
        createUser("testUser", "password123", "테스트유저")

        val loginRequest = UserLoginRequestDto(
            loginId = "testUser",
            password = "password123"
        )

        performAndDocument("로그인 - 성공") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/users/login"
            requestBody = loginRequest
            expectedStatus = status().isOk
            additionalMatchers = arrayOf(
                jsonPath("$.accessToken").isNotEmpty
            )
            snippets = arrayOf(
                requestFields(
                    fieldWithPath("loginId").description("로그인 ID"),
                    fieldWithPath("password").description("비밀번호")
                ),
                responseFields(
                    fieldWithPath("accessToken").description("인증 토큰")
                )
            )
        }
    }
}