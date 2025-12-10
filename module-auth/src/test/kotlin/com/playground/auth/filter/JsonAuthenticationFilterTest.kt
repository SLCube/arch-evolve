package com.playground.auth.filter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.playground.auth.presentation.request.AuthLoginRequestDto
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication

@Suppress("NonAsciiCharacters")
class JsonAuthenticationFilterTest {

    private val authenticationManager: AuthenticationManager = mock()

    private val objectMapper = jacksonObjectMapper()

    private val jsonAuthenticationFilter = JsonAuthenticationFilter(objectMapper, authenticationManager)

    @Test
    fun `JSON 로그인 요청을 UsernamePasswordAuthenticationToken 으로 변환하여 인증한다`() {
        val loginRequest = AuthLoginRequestDto(loginId = "tester", password = "password123")
        val authenticationResult: Authentication = mock()

        given(authenticationManager.authenticate(any()))
            .willReturn(authenticationResult)

        val request =
            MockHttpServletRequest().apply {
                method = HttpMethod.POST.toString()
                contentType = MediaType.APPLICATION_JSON_VALUE
                setContent(objectMapper.writeValueAsBytes(loginRequest))
            }

        val response = MockHttpServletResponse()

        val result = jsonAuthenticationFilter.attemptAuthentication(request, response)

        val captor = argumentCaptor<UsernamePasswordAuthenticationToken>()
        verify(authenticationManager, times(1)).authenticate(captor.capture())

        captor.firstValue.name shouldBe loginRequest.loginId
        captor.firstValue.credentials shouldBe loginRequest.password
        result shouldBe authenticationResult
    }

    @Test
    fun `JSON이 아닌 요청은 기본 UsernamePasswordAuthenticationFilter 로 위임한다`() {
        val authenticationResult: Authentication = mock()

        given(authenticationManager.authenticate(any()))
            .willReturn(authenticationResult)

        val request =
            MockHttpServletRequest().apply {
                method = HttpMethod.POST.toString()
                contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE
                setParameter("username", "formUser")
                setParameter("password", "formPassword")
            }

        val response = MockHttpServletResponse()

        val result = jsonAuthenticationFilter.attemptAuthentication(request, response)

        verify(authenticationManager, times(1)).authenticate(any())
        result shouldBe authenticationResult
    }
}
