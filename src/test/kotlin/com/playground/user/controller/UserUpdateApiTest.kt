package com.playground.user.controller

import com.playground.support.ApiTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import com.playground.user.controller.request.UserNicknameUpdateRequestDto
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
class UserUpdateApiTest: ApiTest() {

    @Test
    fun `닉네임 변경 - 성공`() {
        val user = createUser("testuser", "testuser123", "테스트유저")
        val jwtToken = getAccessToken("testuser", "testuser123")
        val updateRequest = UserNicknameUpdateRequestDto("새로운닉네임")

        performAndDocument("user-update-nickname-success") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/users/{userId}/nickname"
            urlVars = arrayOf(user.id)
            requestBody = updateRequest
            accessToken = jwtToken
            expectedStatus = status().isOk
            snippets = arrayOf(
                pathParameters(
                    parameterWithName("userId").description("사용자 ID")
                ),
                requestFields(
                    fieldWithPath("nickname").description("새로운 닉네임")
                ),
                responseFields(
                    fieldWithPath("loginId").description("로그인 ID"),
                    fieldWithPath("nickname").description("닉네임")
                )
            )
        }

        val updatedUser = userRepository.findById(user.id!!).get()
        updatedUser.nickname shouldBe updateRequest.nickname
        updatedUser.nickname shouldNotBe "테스트유저"
    }

    @Test
    fun `닉네임 변경 - 실패, USER 권한으로 다른 사용자의 닉네임 변경 시도`() {
        createUser("user", "user123", "일반유저")
        val otherUser = createUser("other", "other123", "다른유저")
        val jwtToken = getAccessToken("user", "user123")
        val updateRequest = UserNicknameUpdateRequestDto("새로운닉네임")

        performAndDocument("user-update-nickname-forbidden") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/users/{userId}/nickname"
            urlVars = arrayOf(otherUser.id)
            requestBody = updateRequest
            accessToken = jwtToken
            expectedStatus = status().isForbidden
            snippets = arrayOf(
                responseFields(commonErrorResponseSnippet())
            )
        }
    }

    @Test
    fun `닉네임 변경 - 실패, 동일 닉네임으로 변경 시도`() {
        val user = createUser("user", "user123", "일반유저")
        val jwtToken = getAccessToken("user", "user123")
        val updateRequest = UserNicknameUpdateRequestDto(user.nickname) // 현재 닉네임과 동일

        performAndDocument("user-update-nickname-same") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/users/{userId}/nickname"
            urlVars = arrayOf(user.id)
            requestBody = updateRequest
            accessToken = jwtToken
            expectedStatus = status().isBadRequest
            additionalMatchers = arrayOf(
                jsonPath("$.code").value("SAME_NICKNAME"),
                jsonPath("$.message").value("동일한 닉네임이 존재합니다.")
            )
            snippets = arrayOf(
                responseFields(commonErrorResponseSnippet())
            )
        }
    }

    @Test
    fun `닉네임 변경 - 실패, 다른 사용자가 사용 중인 닉네임으로 변경 시도`() {
        val user = createUser("user", "user123", "일반유저")
        val otherUser = createUser("other", "other123", "다른유저")
        val jwtToken = getAccessToken("user", "user123")
        val updateRequest = UserNicknameUpdateRequestDto(otherUser.nickname) // 다른 유저의 닉네임

        performAndDocument("user-update-nickname-duplicate") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/users/{userId}/nickname"
            urlVars = arrayOf(user.id)
            requestBody = updateRequest
            accessToken = jwtToken
            expectedStatus = status().isConflict
            additionalMatchers = arrayOf(
                jsonPath("$.code").value("DUPLICATE_NICKNAME"),
                jsonPath("$.message").value("이미 존재하는 닉네임입니다.")
            )
            snippets = arrayOf(
                responseFields(commonErrorResponseSnippet())
            )
        }
    }

    @Test
    fun `닉네임 변경 - 실패, 유효하지 않은 닉네임 (길이)`() {
        val user = createUser("user", "user123", "일반유저")
        val jwtToken = getAccessToken("user", "user123")
        val updateRequest = UserNicknameUpdateRequestDto("a") // 닉네임 최소 길이 미달

        performAndDocument("user-update-nickname-invalid") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/users/{userId}/nickname"
            urlVars = arrayOf(user.id)
            requestBody = updateRequest
            accessToken = jwtToken
            expectedStatus = status().isBadRequest
            additionalMatchers = arrayOf(
                jsonPath("$.message").value("입력값이 유효하지 않습니다."),
                jsonPath("$.errors.nickname").value("닉네임은 2자 이상 10자 이하로 입력해주세요.")
            )
            snippets = arrayOf(
                responseFields(
                    commonErrorResponseSnippet() +
                            fieldWithPath("errors.nickname").description("닉네임 필드의 에러 메시지")
                )
            )
        }
    }
}