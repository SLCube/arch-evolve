//package com.playground.auth.security
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.playground.auth.presentation.request.AuthLoginRequestDto
//import com.playground.product.presentation.request.ProductSaveRequestDto
//import com.playground.user.domain.enum.UserRole
//import com.playground.user.persistence.entity.UserJpaEntity
//import com.playground.user.persistence.repository.UserRepository
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.http.MediaType
//import org.springframework.security.crypto.password.PasswordEncoder
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.test.web.servlet.get
//import org.springframework.test.web.servlet.post
//import org.springframework.transaction.annotation.Transactional
//
//@Suppress("NonAsciiCharacters")
//@SpringBootTest
//@Transactional
//@AutoConfigureMockMvc
//class SecurityIntegrationTest(
//    @param:Autowired private val mockMvc: MockMvc,
//    @param:Autowired private val objectMapper: ObjectMapper,
//    @param:Autowired private val userRepository: UserRepository,
//    @param:Autowired private val passwordEncoder: PasswordEncoder,
//) {
//    @Test
//    fun `실제 JWT 토큰으로 인증 - 성공, 보호된 API에 접근할 수 있다`() {
//        val testUserJpaEntity =
//            UserJpaEntity(
//                loginId = "testUser",
//                password = passwordEncoder.encode("password123"),
//                nickname = "테스트유저",
//            )
//        userRepository.save(testUserJpaEntity)
//
//        val loginRequest =
//            AuthLoginRequestDto(
//                loginId = "testUser",
//                password = "password123",
//            )
//
//        val loginResult =
//            mockMvc
//                .post("/users/login") {
//                    contentType = MediaType.APPLICATION_JSON
//                    content = objectMapper.writeValueAsString(loginRequest)
//                }.andExpect {
//                    status { isOk() }
//                }.andReturn()
//
//        val accessToken = objectMapper.readTree(loginResult.response.contentAsString).get("accessToken").asText()
//
//        mockMvc
//            .get("/products") {
//                header("Authorization", "Bearer $accessToken")
//            }.andExpect {
//                status { isOk() }
//            }
//    }
//
//    @Test
//    fun `인증 없이 보호된 API에 접근 - 실패, 401 Unauthorized를 반환한다`() {
//        mockMvc
//            .get("/products")
//            .andExpect {
//                status { isUnauthorized() }
//            }
//    }
//
//    @Test
//    fun `USER 권한으로 ADMIN 전용 API 접근 - 실패, 403 Forbidden을 반환한다`() {
//        val userJpaEntity =
//            UserJpaEntity(
//                loginId = "user",
//                password = passwordEncoder.encode("password123"),
//                nickname = "일반유저",
//                role = UserRole.USER,
//            )
//        userRepository.save(userJpaEntity)
//
//        val loginRequest =
//            AuthLoginRequestDto(
//                loginId = "user",
//                password = "password123",
//            )
//        val loginResult =
//            mockMvc
//                .post("/users/login") {
//                    contentType = MediaType.APPLICATION_JSON
//                    content = objectMapper.writeValueAsString(loginRequest)
//                }.andExpect {
//                    status { isOk() }
//                }.andReturn()
//
//        val accessToken = objectMapper.readTree(loginResult.response.contentAsString).get("accessToken").asText()
//
//        val requestDto =
//            ProductSaveRequestDto(
//                name = "새 상품",
//                stock = 10,
//                price = 10000.toBigDecimal(),
//            )
//
//        mockMvc
//            .post("/products") {
//                header("Authorization", "Bearer $accessToken")
//                contentType = MediaType.APPLICATION_JSON
//                content = requestDto
//            }.andExpect {
//                status { isForbidden() }
//            }
//    }
//}
