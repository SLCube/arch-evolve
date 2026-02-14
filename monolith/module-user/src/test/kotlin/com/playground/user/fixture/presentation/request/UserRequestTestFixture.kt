package com.playground.user.fixture.presentation.request

import com.playground.user.presentation.request.UserNicknameUpdateRequestDto
import com.playground.user.presentation.request.UserPasswordUpdateRequestDto
import com.playground.user.presentation.request.UserSignUpRequestDto

object UserRequestTestFixture {
    fun mockSignUpRequest(
        loginId: String = "testUser",
        password: String = "password123",
        nickname: String = "테스트유저"
    ) = UserSignUpRequestDto(
        loginId = loginId,
        password = password,
        nickname = nickname,
    )

    fun mockUserNicknamdUpdateRequest(
        nickname: String = "새로운닉네임"
    ) = UserNicknameUpdateRequestDto(
        nickname = nickname,
    )

    fun mockUserPasswordUpdateRequest(
        oldPassword: String = "oldPassword",
        newPassword: String = "newPassword"
    ) = UserPasswordUpdateRequestDto(
        oldPassword = oldPassword,
        newPassword = newPassword,
    )
}