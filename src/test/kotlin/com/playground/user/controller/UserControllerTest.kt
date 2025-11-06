package com.playground.user.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.user.controller.request.UserLoginRequestDto
import com.playground.user.controller.request.UserSignUpRequestDto
import com.playground.user.domain.User
import com.playground.user.repository.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest : BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private val signUpRequest = UserSignUpRequestDto(
        loginId = "testUser",
        password = "password123",
        nickname = "테스트유저"
    )

    init {
        afterTest {
            userRepository.deleteAll()
        }

        Given("회원가입 기능 테스트") {
            When("정상적인 정보로 회원가입을 요청하면") {
                val resultActions = mockMvc.post("/users/sign-up") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(signUpRequest)
                }

                Then("성공적으로 사용자가 생성되고 201 Created 상태를 반환한다") {
                    resultActions.andExpect {
                        status { isCreated() }
                        jsonPath("$.loginId") { value(signUpRequest.loginId) }
                        jsonPath("$.nickname") { value(signUpRequest.nickname) }
                    }

                    val savedUser = userRepository.findByLoginId(signUpRequest.loginId).get()
                    savedUser.password shouldNotBe signUpRequest.password
                }
            }

            When("loginId가 비어있는 상태로 요청하면") {
                val invalidRequest = signUpRequest.copy(loginId = "")

                val resultActions = mockMvc.post("/users/sign-up") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(invalidRequest)
                }

                Then("400 Bad Request 응답과 함께, 올바른 에러 메시지를 반환한다") {
                    resultActions.andExpect {
                        status { isBadRequest() }
                        jsonPath("$.message") { value("입력값이 유효하지 않습니다.") }
                        jsonPath("$.errors.loginId") { value("로그인 ID는 4자 이상 20자 이하로 입력해주세요.") }
                    }
                }
            }

            When("password가 8자 미만으로 요청하면") {
                val invalidRequest = signUpRequest.copy(password = "short")

                val resultActions = mockMvc.post("/users/sign-up") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(invalidRequest)
                }

                Then("400 Bad Request 응답과 함께, 올바른 에러 메시지를 반환한다") {
                    resultActions.andExpect {
                        status { isBadRequest() }
                        jsonPath("$.message") { value("입력값이 유효하지 않습니다.") }
                        jsonPath("$.errors.password") { value("비밀번호는 8자 이상 16자 이하로 입력해주세요.") }
                    }
                }
            }

            When("loginId가 비어있고, password가 8자 미만으로 요청하면") {
                val invalidRequest = signUpRequest.copy(loginId = "", password = "short")

                val resultActions = mockMvc.post("/users/sign-up") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(invalidRequest)
                }

                Then("400 Bad Request 응답과 함께, 두 필드의 에러 메시지를 모두 반환한다") {
                    resultActions.andExpect {
                        status { isBadRequest() }
                        jsonPath("$.message") { value("입력값이 유효하지 않습니다.") }
                        jsonPath("$.errors.loginId") { value("로그인 ID는 4자 이상 20자 이하로 입력해주세요.") }
                        jsonPath("$.errors.password") { value("비밀번호는 8자 이상 16자 이하로 입력해주세요.") }
                    }
                }
            }
        }

        Given("로그인 기능 테스트") {
            userRepository.save(
                User(
                    loginId = signUpRequest.loginId,
                    password = passwordEncoder.encode(signUpRequest.password),
                    nickname = signUpRequest.nickname
                )
            )

            When("올바른 loginId와 password로 로그인을 요청하면") {
                val loginRequest = UserLoginRequestDto(
                    loginId = signUpRequest.loginId,
                    password = signUpRequest.password
                )

                val resultActions = mockMvc.post("/users/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(loginRequest)
                }

                Then("성공적으로 로그인되고 accessToken이 포함된 200 OK 상태를 반환한다") {
                    resultActions.andExpect {
                        status { isOk() }
                        jsonPath("$.accessToken") { isNotEmpty() }
                    }
                }
            }
        }
    }
}