package com.playground.common.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.user.controller.request.UserLoginRequestDto
import com.playground.user.domain.User
import com.playground.user.repository.UserRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@Suppress("NonAsciiCharacters")
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest(
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
    fun `실제 JWT 토큰으로 인증 - 성공, 보호된 API에 접근할 수 있다`() {
        val testUser = User(
            loginId = "testUser",
            password = passwordEncoder.encode("password123"),
            nickname = "테스트유저"
        )
        userRepository.save(testUser)

        val loginRequest = UserLoginRequestDto(
            loginId = "testUser",
            password = "password123"
        )
        val loginResult = mockMvc.post("/users/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginRequest)
        }.andExpect {
            status { isOk() }
        }.andReturn()

        val accessToken = objectMapper.readTree(loginResult.response.contentAsString).get("accessToken").asText()

        mockMvc.get("/products") {
            header("Authorization", "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `인증 없이 보호된 API에 접근 - 실패, 401 Unauthorized를 반환한다`() {
        mockMvc.get("/products")
            .andExpect {
                status { isUnauthorized() }
            }
    }
}