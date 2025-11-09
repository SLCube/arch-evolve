package com.playground.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.auth.presentation.response.AuthTokenResponseDto
import com.playground.auth.presentation.request.AuthLoginRequestDto
import com.playground.user.persistence.entity.User
import com.playground.user.domain.enum.UserRole
import com.playground.user.persistence.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension::class)
abstract class ApiTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var webApplicationContext: WebApplicationContext

    lateinit var restDocsMockMvc: MockMvc

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        this.restDocsMockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply {
                apply<DefaultMockMvcBuilder>(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                apply<DefaultMockMvcBuilder>(springSecurity())
            }
            .build()
    }

    @AfterEach
    fun cleanUpUser() {
        userRepository.deleteAll()
    }

    protected fun createUser(loginId: String, password: String, nickname: String, role: UserRole = UserRole.USER): User {
        val user = User(
            loginId = loginId,
            password = passwordEncoder.encode(password),
            nickname = nickname,
            role = role
        )

        return userRepository.save(user)
    }

    protected fun getAccessToken(loginId: String, password: String): String {
        val loginRequest = AuthLoginRequestDto(loginId, password)
        val loginResult = mockMvc.post("/users/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginRequest)
        }.andExpect {
            status { isOk() }
        }.andReturn()

        return objectMapper.readValue(
            loginResult.response.contentAsString,
            AuthTokenResponseDto::class.java
        ).accessToken
    }
}