package com.playground.user.presentation.controller

import com.playground.common.error.ErrorCode
import com.playground.support.RestDocsTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import com.playground.user.application.port.inbound.UserUseCase
import com.playground.user.domain.exception.DuplicateLoginIdException
import com.playground.user.fixture.application.domain.UserDomainTestFixture
import com.playground.user.fixture.presentation.request.UserRequestTestFixture
import com.playground.user.presentation.annotation.UserControllerSliceTest
import com.playground.user.presentation.mapper.toCommand
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@UserControllerSliceTest
class UserSignUpApiTest(
    @param:Autowired private val userUseCase: UserUseCase,
): RestDocsTest() {

    @Test
    fun `회원가입 성공`() {
        // given
        val request = UserRequestTestFixture.mockSignUpRequest()

        val mockUser = UserDomainTestFixture.mockUser()

        given(userUseCase.signUp(request.toCommand()))
            .willReturn(mockUser)

        // when & then
        performAndDocument("회원가입 성공") {
            tag = "사용자 API"
            summary = "회원가입"
            description = "새로운 사용자를 등록합니다. loginId, password, nickname을 입력받아 회원가입을 진행합니다."
            httpMethod = HttpMethod.POST
            urlTemplate = "/users/sign-up"
            requestBody = request
            expectedStatus = status().isCreated
            additionalMatchers = arrayOf(
                jsonPath("$.loginId").value(request.loginId),
                jsonPath("$.nickname").value(request.nickname),
            )
            snippets =
                arrayOf(
                    requestFields(
                        fieldWithPath("loginId").description("로그인 ID"),
                        fieldWithPath("password").description("비밀번호"),
                        fieldWithPath("nickname").description("닉네임"),
                    ),
                    responseFields(
                        fieldWithPath("loginId").description("로그인 ID"),
                        fieldWithPath("nickname").description("닉네임"),
                    ),
                )
        }
    }

    @Test
    fun `회원가입 실패 - loginId가 비어있음`() {
        // given
        val request = UserRequestTestFixture.mockSignUpRequest(loginId = "")

        // when & then
        performAndDocument("회원가입 실패 - loginId가 비어있음") {
            tag = "사용자 API"
            summary = "회원가입"
            description = "새로운 사용자를 등록합니다. loginId, password, nickname을 입력받아 회원가입을 진행합니다."
            httpMethod = HttpMethod.POST
            urlTemplate = "/users/sign-up"
            requestBody = request
            expectedStatus = status().isBadRequest
            additionalMatchers = arrayOf(
                jsonPath("$.message").value("입력값이 유효하지 않습니다."),
                jsonPath("$.errors.loginId").value(matchesPattern("로그인 ID는 비어있을 수 없습니다.|로그인 ID는 4자 이상 20자 이하로 입력해주세요.")),
            )
            snippets =
                arrayOf(
                    responseFields(
                        commonErrorResponseSnippet() +
                            fieldWithPath("errors.loginId").description("로그인 ID 필드의 에러 메시지"),
                    )
                )
        }
    }

    @Test
    fun `회원가입 실패 - loginId 중복`() {
        // given
        val existingLoginId = "existingUser"
        val request = UserRequestTestFixture.mockSignUpRequest(loginId = existingLoginId)

        given(userUseCase.signUp(request.toCommand()))
            .willThrow(DuplicateLoginIdException(existingLoginId))

        // when & then
        performAndDocument("회원가입 실패 - loginId 중복") {
            tag = "사용자 API"
            summary = "회원가입"
            description = "새로운 사용자를 등록합니다. loginId, password, nickname을 입력받아 회원가입을 진행합니다."
            httpMethod = HttpMethod.POST
            urlTemplate = "/users/sign-up"
            requestBody = request
            expectedStatus = status().isConflict
            additionalMatchers = arrayOf(
                jsonPath("$.code").value(ErrorCode.DUPLICATE_LOGIN_ID.code),
                jsonPath("$.message").value(ErrorCode.DUPLICATE_LOGIN_ID.message(existingLoginId)),
            )
            snippets = arrayOf(
                responseFields(commonErrorResponseSnippet()),
            )
        }
    }
}