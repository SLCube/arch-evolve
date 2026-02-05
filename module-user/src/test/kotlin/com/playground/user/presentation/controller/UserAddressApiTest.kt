package com.playground.user.presentation.controller

import com.playground.support.RestDocsTest
import com.playground.support.docs.performAndDocument
import com.playground.support.security.annotation.WithMockAuthUser
import com.playground.user.application.port.inbound.UserAddressUseCase
import com.playground.user.fixture.presentation.request.AddressRequestTestFixture
import com.playground.user.presentation.annotation.UserAddressControllerSliceTest
import com.playground.user.presentation.mapper.toAddressDefaultSetCommand
import com.playground.user.presentation.mapper.toCommand
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@UserAddressControllerSliceTest
class UserAddressApiTest(
    @param:Autowired private val userAddressUseCase: UserAddressUseCase,
) : RestDocsTest() {

    @BeforeEach
    fun setUp() {
        reset(userAddressUseCase)
    }

    @Test
    @WithMockAuthUser(userId = 1L)
    fun `주소 등록 - 성공`() {
        val userId = 1L
        val request = AddressRequestTestFixture.mockAddressRegisterRequest()
        val expectedCommand = request.toCommand(userId)

        performAndDocument("주소 등록 - 성공") {
            tag = "사용자 API"
            summary = "배송지 주소 등록"
            description = "인증된 사용자의 배송지 주소를 등록합니다. 수령인 정보와 주소 정보를 입력받아 새로운 배송지를 생성합니다."
            httpMethod = HttpMethod.POST
            urlTemplate = "/users/addresses"
            requestBody = request
            expectedStatus = status().isCreated
            snippets = arrayOf(
                requestFields(
                    fieldWithPath("receiverName").description("수령인 이름"),
                    fieldWithPath("receiverPhoneNumber").description("수령인 연락처"),
                    fieldWithPath("zipCode").description("우편번호"),
                    fieldWithPath("baseAddress").description("기본 주소"),
                    fieldWithPath("detailAddress").description("상세 주소"),
                ),
            )
        }

        verify(userAddressUseCase).registerAddress(expectedCommand)
    }

    @Test
    @WithMockAuthUser(userId = 1L)
    fun `기본 주소 설정 - 성공`() {
        val addressId = 10L
        val expectedCommand = toAddressDefaultSetCommand(
            userId = 1L,
            addressId = addressId,
        )

        performAndDocument("기본 주소 설정 - 성공") {
            tag = "사용자 API"
            summary = "기본 배송지 설정"
            description = "인증된 사용자의 기본 배송지를 설정합니다. addressId를 통해 특정 배송지를 기본 배송지로 지정합니다."
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/users/addresses/{addressId}/default"
            urlVars = arrayOf(addressId)
            expectedStatus = status().isNoContent
            snippets = arrayOf(
                pathParameters(
                    parameterWithName("addressId").description("기본 주소로 지정할 주소 ID"),
                ),
            )
        }

        verify(userAddressUseCase).setDefaultAddress(expectedCommand)
    }
}
