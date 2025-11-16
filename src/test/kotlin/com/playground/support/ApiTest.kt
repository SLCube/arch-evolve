package com.playground.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.auth.presentation.request.AuthLoginRequestDto
import com.playground.auth.presentation.response.AuthTokenResponseDto
import com.playground.user.application.port.inbound.UserUseCase
import com.playground.user.application.port.inbound.command.SignUpCommand
import com.playground.user.domain.model.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension::class)
abstract class ApiTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var userUseCase: UserUseCase

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    lateinit var restDocsMockMvc: MockMvc

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        this.restDocsMockMvc =
            MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply {
                    apply<DefaultMockMvcBuilder>(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                    apply<DefaultMockMvcBuilder>(springSecurity())
                }.build()
    }

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
