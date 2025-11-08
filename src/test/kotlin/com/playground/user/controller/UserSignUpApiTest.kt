package com.playground.user.controller

import com.playground.support.ApiTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import com.playground.user.controller.request.UserSignUpRequestDto
import io.kotest.matchers.shouldNotBe
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
class UserSignUpApiTest : ApiTest() {

    @Test
    fun `회원가입 - 성공`() {
        val signUpRequest = UserSignUpRequestDto(
            loginId = "testUser",
            password = "password123",
            nickname = "테스트유저"
        )

        performAndDocument("user-sign-up-success") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/users/sign-up"
            requestBody = signUpRequest
            expectedStatus = status().isCreated
            additionalMatchers = arrayOf(
                jsonPath("$.loginId").value(signUpRequest.loginId),
                jsonPath("$.nickname").value(signUpRequest.nickname),
            )
            snippets = arrayOf(
                requestFields(
                    fieldWithPath("loginId").description("로그인 ID"),
                    fieldWithPath("password").description("비밀번호"),
                    fieldWithPath("nickname").description("닉네임")
                ),
                responseFields(
                    fieldWithPath("loginId").description("로그인 ID"),
                    fieldWithPath("nickname").description("닉네임")
                )
            )
        }

        val savedUser = userRepository.findByLoginId(signUpRequest.loginId).get()
        savedUser.password shouldNotBe signUpRequest.password
    }

    @Test
    fun `회원가입 - 실패, loginId가 비어있음`() {
        val signUpRequest = UserSignUpRequestDto(
            loginId = "",
            password = "password123",
            nickname = "테스트유저"
        )

        performAndDocument("user-sign-up-validation-blank-login-id") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/users/sign-up"
            requestBody = signUpRequest
            expectedStatus = status().isBadRequest
            additionalMatchers = arrayOf(
                jsonPath("$.message").value("입력값이 유효하지 않습니다."),
                jsonPath("$.errors.loginId").value(matchesPattern("로그인 ID는 비어있을 수 없습니다.|로그인 ID는 4자 이상 20자 이하로 입력해주세요."))
            )
            snippets = arrayOf(
                responseFields(
                    commonErrorResponseSnippet() +
                            fieldWithPath("errors.loginId").description("로그인 ID 필드의 에러 메시지")
                )
            )
        }
    }

    @Test
    fun `회원가입 - 실패, password가 8자 미만`() {
        val signUpRequest = UserSignUpRequestDto(
            loginId = "testUser",
            password = "short",
            nickname = "테스트유저"
        )

        performAndDocument("user-sign-up-validation-short-password") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/users/sign-up"
            requestBody = signUpRequest
            expectedStatus = status().isBadRequest
            additionalMatchers = arrayOf(
                jsonPath("$.message").value("입력값이 유효하지 않습니다."),
                jsonPath("$.errors.password").value("비밀번호는 8자 이상 16자 이하로 입력해주세요.")
            )
            snippets = arrayOf(
                responseFields(
                    commonErrorResponseSnippet() +
                            fieldWithPath("errors.password").description("비밀번호 필드의 에러 메시지")
                )
            )
        }
    }
}