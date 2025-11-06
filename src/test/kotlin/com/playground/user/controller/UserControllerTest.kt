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
        username = "testUser",
        password = "password123",
        nickname = "테스트유저"
    )

    init {
        afterTest {
            userRepository.deleteAll()
        }

        Given("회원가입에 필요한 정보가 주어졌을 때") {
            When("회원가입 API를 요청하면") {
                val resultActions = mockMvc.post("/users/sign-up") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(signUpRequest)
                }

                Then("성공적으로 사용자가 생성되고 201 Created 상태를 반환한다") {
                    resultActions.andExpect {
                        status { isCreated() }
                        jsonPath("$.username") { value(signUpRequest.username) }
                        jsonPath("$.nickname") { value(signUpRequest.nickname) }
                    }

                    val savedUser = userRepository.findByUsername(signUpRequest.username).get()
                    savedUser.password shouldNotBe signUpRequest.password
                }
            }
        }

        Given("회원가입이 완료된 사용자가 존재할 때") {
            userRepository.save(User(
                username = signUpRequest.username,
                password = passwordEncoder.encode(signUpRequest.password),
                nickname = signUpRequest.nickname
            ))

            When("올바른 username과 password로 로그인을 요청하면") {
                val loginRequest = UserLoginRequestDto(
                    username = signUpRequest.username,
                    password = signUpRequest.password
                )

                val resultActions = mockMvc.post("/users/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(loginRequest)
                }

                Then("성공적으로 로그인되고 200 OK 상태를 반환한다") {
                    resultActions.andExpect {
                        status { isOk() }
                        jsonPath("$.accessToken") { isNotEmpty() }
                    }
                }
            }
        }
    }
}