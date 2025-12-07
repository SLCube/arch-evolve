package com.playground.user.fixture.presentation.request

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
}