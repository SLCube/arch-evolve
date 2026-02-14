package com.playground.auth.jwt

import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder

@Suppress("NonAsciiCharacters")
class JwtAuthenticationFilterTest {

    private val jwtTokenProvider: JwtTokenProvider = mock()

    private val jwtAuthenticationFilter = JwtAuthenticationFilter(jwtTokenProvider)

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `유효한 토큰이 있으면 SecurityContext 에 Authentication 을 저장한다`() {
        val token = "valid.jwt.token"
        val authentication: Authentication = mock()

        given(jwtTokenProvider.validateToken(token)).willReturn(true)
        given(jwtTokenProvider.getAuthentication(token)).willReturn(authentication)

        val request =
            MockHttpServletRequest().apply {
                addHeader("Authorization", "Bearer $token")
            }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        SecurityContextHolder.getContext().authentication shouldBe authentication
        verify(jwtTokenProvider).validateToken(token)
        verify(jwtTokenProvider).getAuthentication(token)
    }

    @Test
    fun `토큰이 없거나 유효하지 않으면 SecurityContext 를 비워둔다`() {
        val invalidToken = "expired.jwt.token"

        given(jwtTokenProvider.validateToken(invalidToken)).willReturn(false)

        val request =
            MockHttpServletRequest().apply {
                addHeader("Authorization", "Bearer $invalidToken")
            }
        val response = MockHttpServletResponse()
        val filterChain = MockFilterChain()

        jwtAuthenticationFilter.doFilter(request, response, filterChain)

        SecurityContextHolder.getContext().authentication.shouldBeNull()
        verify(jwtTokenProvider).validateToken(invalidToken)
        verify(jwtTokenProvider, never()).getAuthentication(any())
    }
}
