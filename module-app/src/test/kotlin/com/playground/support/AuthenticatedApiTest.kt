package com.playground.support

import com.playground.auth.presentation.request.AuthLoginRequestDto
import com.playground.auth.presentation.response.AuthTokenResponseDto
import com.playground.user.application.port.inbound.UserUseCase
import com.playground.user.application.port.inbound.command.SignUpCommand
import com.playground.user.domain.model.User
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post

abstract class AuthenticatedApiTest: ApiTest() {
    lateinit var userUseCase: UserUseCase

    protected fun createUser(
        loginId: String,
        password: String,
        nickname: String,
    ): User {
        val command =
            SignUpCommand(
                loginId = loginId,
                password = password,
                nickname = nickname,
            )

        return userUseCase.signUp(command)
    }

    protected fun getAccessToken(
        loginId: String,
        password: String,
    ): String {
        val loginRequest = AuthLoginRequestDto(loginId, password)
        val loginResult =
            mockMvc
                .post("/users/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(loginRequest)
                }.andExpect {
                    status { isOk() }
                }.andReturn()

        return objectMapper
            .readValue(
                loginResult.response.contentAsString,
                AuthTokenResponseDto::class.java,
            ).accessToken
    }
}