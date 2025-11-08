package com.playground.user.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.common.security.response.AuthTokenResponseDto
import com.playground.user.controller.request.UserLoginRequestDto
import com.playground.user.controller.request.UserNicknameUpdateRequestDto
import com.playground.user.controller.request.UserSignUpRequestDto
import com.playground.user.domain.User
import com.playground.user.enum.UserRole
import com.playground.user.repository.UserRepository
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.matches
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@Suppress("NonAsciiCharacters")
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(RestDocumentationExtension::class)
class UserControllerTest(
    @param:Autowired private val mockMvc: MockMvc,
    @param:Autowired private val objectMapper: ObjectMapper,
    @param:Autowired private val userRepository: UserRepository,
    @param:Autowired private val passwordEncoder: PasswordEncoder,
    @param:Autowired private val webApplicationContext: WebApplicationContext
) {

    private lateinit var restDocsMockMvc: MockMvc

    @BeforeEach
    fun setUp(restDocumentation: RestDocumentationContextProvider) {
        this.restDocsMockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply {
                apply<DefaultMockMvcBuilder>(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                apply<DefaultMockMvcBuilder>(springSecurity())
            }
            .build()
    }

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
            jsonPath("$.errors.loginId") {
                matches("로그인 ID는 필수 입력값입니다.|로그인 ID는 4자 이상 20자 이하로 입력해주세요.")
            }
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
        createUser("testUser", "password123", "테스트유저")

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

    @Test
    fun `닉네임 변경 - 성공`() {
        val user = createUser("testuser", "testuser123", "테스트유저")
        val accessToken = getAccessToken("testuser", "testuser123")
        val updateRequest = UserNicknameUpdateRequestDto("새로운닉네임")

        restDocsMockMvc.patch("/users/{userId}/nickname", user.id) {
            header("Authorization", "Bearer $accessToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updateRequest)
        }
            .andExpect {
                status { isOk() }
            }
            .andDo {
                document(
                    "user-update-nickname-success",
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
        val accessToken = getAccessToken("user", "user123")
        val updateRequest = UserNicknameUpdateRequestDto("새로운닉네임")

        mockMvc.patch("/users/{userId}/nickname", otherUser.id) {
            header("Authorization", "Bearer $accessToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updateRequest)
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `닉네임 변경 - 실패, 동일 닉네임으로 변경 시도`() {
        val user = createUser("user", "user123", "일반유저")
        val accessToken = getAccessToken("user", "user123")
        val updateRequest = UserNicknameUpdateRequestDto(user.nickname) // 현재 닉네임과 동일

        mockMvc.patch("/users/{userId}/nickname", user.id) {
            header("Authorization", "Bearer $accessToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updateRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value("SAME_NICKNAME") }
            jsonPath("$.message") { value("동일한 닉네임이 존재합니다.") }
        }
    }

    @Test
    fun `닉네임 변경 - 실패, 다른 사용자가 사용 중인 닉네임으로 변경 시도`() {
        val user = createUser("user", "user123", "일반유저")
        val otherUser = createUser("other", "other123", "다른유저")
        val accessToken = getAccessToken("user", "user123")
        val updateRequest = UserNicknameUpdateRequestDto(otherUser.nickname) // 다른 유저의 닉네임

        mockMvc.patch("/users/{userId}/nickname", user.id) {
            header("Authorization", "Bearer $accessToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updateRequest)
        }.andExpect {
            status { isConflict() }
            jsonPath("$.code") { value("DUPLICATE_NICKNAME") }
            jsonPath("$.message") { value("이미 존재하는 닉네임입니다.") }
        }
    }

    @Test
    fun `닉네임 변경 - 실패, 유효하지 않은 닉네임 (길이)`() {
        val user = createUser("user", "user123", "일반유저")
        val accessToken = getAccessToken("user", "user123")
        val updateRequest = UserNicknameUpdateRequestDto("a") // 닉네임 최소 길이 미달

        mockMvc.patch("/users/{userId}/nickname", user.id) {
            header("Authorization", "Bearer $accessToken")
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updateRequest)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.message") { value("입력값이 유효하지 않습니다.") }
            jsonPath("$.errors.nickname") { value("닉네임은 2자 이상 10자 이하로 입력해주세요.") }
        }
    }

    private fun createUser(loginId: String, password: String, nickname: String, role: UserRole = UserRole.USER): User {
        val user = User(
            loginId = loginId,
            password = passwordEncoder.encode(password),
            nickname = nickname,
            role = role
        )
        return userRepository.save(user)
    }

    private fun getAccessToken(loginId: String, password: String): String {
        val loginRequest = UserLoginRequestDto(loginId, password)
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