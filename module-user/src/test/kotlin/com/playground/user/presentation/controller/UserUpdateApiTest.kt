package com.playground.user.presentation.controller

import com.playground.common.error.ErrorCode
import com.playground.support.RestDocsTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import com.playground.support.security.annotation.WithMockAuthUser
import com.playground.user.application.port.inbound.UserUseCase
import com.playground.user.domain.exception.DuplicateNicknameException
import com.playground.user.domain.exception.PasswordMismatchException
import com.playground.user.fixture.application.domain.UserDomainTestFixture
import com.playground.user.fixture.presentation.request.UserRequestTestFixture
import com.playground.user.presentation.annotation.UserControllerSliceTest
import com.playground.user.presentation.mapper.toCommand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.reset
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@UserControllerSliceTest
class UserUpdateApiTest(
    @param:Autowired private val userUseCase: UserUseCase,
): RestDocsTest() {

    @BeforeEach
    fun setUp() {
        reset(userUseCase)
    }

    @Test
    @WithMockAuthUser(userId = 2L)
    fun `닉네임 변경 - 성공`() {
        // given
        val userId = 2L
        val request = UserRequestTestFixture.mockUserNicknamdUpdateRequest()
        val mockUser = UserDomainTestFixture.mockUser(
            nickname = request.nickname
        )

        given(userUseCase.updateNickname(request.toCommand(userId)))
            .willReturn(mockUser)

        // when & then
        performAndDocument("닉네임 변경 - 성공") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/users/nickname"
            requestBody = request
            expectedStatus = status().isOk
            additionalMatchers = arrayOf(
                jsonPath("$.loginId").value(mockUser.loginId),
                jsonPath("$.nickname").value(request.nickname),
            )
            snippets = arrayOf(
                requestFields(
                    fieldWithPath("nickname").description("새로운 닉네임"),
                ),
                responseFields(
                    fieldWithPath("loginId").description("로그인 ID"),
                    fieldWithPath("nickname").description("닉네임"),
                )
            )
        }
    }

    @Test
    @WithMockAuthUser(userId = 2L)
    fun `닉네임 변경 - 실패, 다른 사용자가 사용 중인 닉네임으로 변경 시도`() {
        // given
        val userId = 2L
        val request = UserRequestTestFixture.mockUserNicknamdUpdateRequest()

        given(userUseCase.updateNickname(request.toCommand(userId)))
            .willThrow(DuplicateNicknameException())

        // when & then
        performAndDocument("닉네임 변경 - 실패, 다른 사용자가 사용 중인 닉네임으로 변경 시도") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/users/nickname"
            requestBody = request
            expectedStatus = status().isConflict
            additionalMatchers =
                arrayOf(
                    jsonPath("$.code").value(ErrorCode.DUPLICATE_NICKNAME.code),
                    jsonPath("$.message").value(ErrorCode.DUPLICATE_NICKNAME.message()),
                )
            snippets = arrayOf(
                responseFields(commonErrorResponseSnippet())
            )
        }
    }

    @Test
    @WithMockAuthUser(userId = 2L)
    fun `닉네임 변경 - 실패, 유효하지 않는 닉네임 (길이)`() {
        // given
        val request = UserRequestTestFixture.mockUserNicknamdUpdateRequest(
            nickname = "testLongerNickname"
        )

        // when & then
        performAndDocument("닉네임 변경 - 실패, 유효하지 않은 닉네임 (길이)") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/users/nickname"
            requestBody = request
            expectedStatus = status().isBadRequest
            additionalMatchers =
                arrayOf(
                    jsonPath("$.message").value("입력값이 유효하지 않습니다."),
                    jsonPath("$.errors.nickname").value("닉네임은 2자 이상 10자 이하로 입력해주세요."),
                )
            snippets = arrayOf(
                responseFields(
                    commonErrorResponseSnippet() +
                            fieldWithPath("errors.nickname").description("닉네임 필드의 에러 메시지"),
                )
            )
        }
    }

    @Test
    @WithMockAuthUser(userId = 2L)
    fun `비밀번호 변경 - 성공`() {
        // given
        val userId = 2L
        val request = UserRequestTestFixture.mockUserPasswordUpdateRequest()

        val mockUser = UserDomainTestFixture.mockUser(
            password = request.newPassword
        )

        given(userUseCase.updatePassword(request.toCommand(userId)))
            .willReturn(mockUser)

        // when & then
        performAndDocument("비밀번호 변경 - 성공") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/users/password"
            requestBody = request
            expectedStatus = status().isOk
            additionalMatchers = arrayOf(
                jsonPath("$.loginId").value(mockUser.loginId),
                jsonPath("$.nickname").value(mockUser.nickname),
            )
            snippets = arrayOf(
                requestFields(
                    fieldWithPath("oldPassword").description("기존 비밀번호"),
                    fieldWithPath("newPassword").description("새 비밀번호"),
                ),
                responseFields(
                    fieldWithPath("loginId").description("로그인 ID"),
                    fieldWithPath("nickname").description("닉네임")
                ),
            )
        }
    }

    @Test
    @WithMockAuthUser(userId = 2L)
    fun `비밀번호 변경 - 실패, 기존 비밀번호 불일치`() {
        // given
        val userId = 2L
        val request = UserRequestTestFixture.mockUserPasswordUpdateRequest()

        given(userUseCase.updatePassword(request.toCommand(userId)))
            .willThrow(PasswordMismatchException())

        // when & then
        performAndDocument("비밀번호 변경 - 실패, 기존 비밀번호 불일치") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/users/password"
            requestBody = request
            expectedStatus = status().isBadRequest
            additionalMatchers =
                arrayOf(
                    jsonPath("$.code").value(ErrorCode.PASSWORD_MISMATCH.code),
                    jsonPath("$.message").value(ErrorCode.PASSWORD_MISMATCH.message()),
                )
            snippets = arrayOf(
                responseFields(commonErrorResponseSnippet())
            )
        }
    }

    @Test
    @WithMockAuthUser(userId = 2L)
    fun `비밀번호 변경 - 실패, 유효하지 않는 비밀번호(길이)`() {
        // given
        val request = UserRequestTestFixture.mockUserPasswordUpdateRequest(
            newPassword = "short"
        )

        // when & then
        performAndDocument("비밀번호 변경 - 실패, 새 비밀번호 유효성 검증 실패 (길이)") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/users/password"
            requestBody = request
            expectedStatus = status().isBadRequest
            additionalMatchers =
                arrayOf(
                    jsonPath("$.message").value("입력값이 유효하지 않습니다."),
                    jsonPath("$.errors.newPassword").value("새 비밀번호는 8자 이상 16자 이하로 입력해주세요."),
                )
            snippets = arrayOf(
                responseFields(
                    commonErrorResponseSnippet() +
                            fieldWithPath("errors.newPassword").description("새 비밀번호 필드의 에러 메시지"),
                )
            )
        }
    }
}