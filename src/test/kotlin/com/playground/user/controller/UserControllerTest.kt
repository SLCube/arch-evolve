package com.playground.user.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.user.controller.request.UserLoginRequestDto
import com.playground.user.controller.request.UserSignUpRequestDto
import com.playground.user.domain.User
import com.playground.user.repository.UserRepository
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@Suppress("NonAsciiCharacters")
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest(
    @param:Autowired private val mockMvc: MockMvc,
    @param:Autowired private val objectMapper: ObjectMapper,
    @param:Autowired private val userRepository: UserRepository,
    @param:Autowired private val passwordEncoder: PasswordEncoder
) {

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
    }

    @Test
    fun `회원가입 - 성공`() {
        val signUpRequest = UserSignUpRequestDto(
            loginId = "testUser",
            password = "password123",
            nickname = "테스트유저"
        )

        mockMvc.post("/users/sign-up") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(signUpRequest)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.loginId") { value(signUpRequest.loginId) }
            jsonPath("$.nickname") { value(signUpRequest.nickname) }
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

        mockMvc.post("/users/sign-up") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(signUpRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.message") { value("입력값이 유효하지 않습니다.") }
            jsonPath("$.errors.loginId") { value("로그인 ID는 4자 이상 20자 이하로 입력해주세요.") }
        }
    }

    @Test
    fun `회원가입 - 실패, password가 8자 미만`() {
        val signUpRequest = UserSignUpRequestDto(
            loginId = "testUser",
            password = "short",
            nickname = "테스트유저"
        )

        mockMvc.post("/users/sign-up") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(signUpRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.message") { value("입력값이 유효하지 않습니다.") }
            jsonPath("$.errors.password") { value("비밀번호는 8자 이상 16자 이하로 입력해주세요.") }
        }
    }

    @Test
    fun `로그인 - 성공`() {
        userRepository.save(
            User(
                loginId = "testUser",
                password = passwordEncoder.encode("password123"),
                nickname = "테스트유저"
            )
        )

        val loginRequest = UserLoginRequestDto(
            loginId = "testUser",
            password = "password123"
        )

        mockMvc.post("/users/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.accessToken") { isNotEmpty() }
        }
    }
}