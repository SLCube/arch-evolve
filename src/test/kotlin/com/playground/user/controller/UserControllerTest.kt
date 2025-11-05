package com.playground.user.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.user.controller.request.UserSignUpRequestDto
import com.playground.user.repository.UserRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest: BehaviorSpec() {

    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    init {
        Given("회원가입에 필요한 정보가 주어졌을 때") {
            val request = UserSignUpRequestDto(
                username = "testUser",
                password = "password123",
                nickname = "테스트유저"
            )

            When("회원가입 API를 요청하면") {
                val resultActions = mockMvc.post("/users/sign-up") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }

                Then("성공적으로 사용자가 생성되고 201 Created 상태를 반환한다") {
                    resultActions.andExpect {
                        status { isCreated() }
                        jsonPath("$.username") { value("testUser") }
                        jsonPath("$.nickname") { value("테스트유저") }
                    }

                    val savedUser = userRepository.findAll().first()
                    savedUser.password shouldNotBe request.password
                }
            }
        }
    }
}