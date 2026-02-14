package com.playground.auth.application.service

import com.playground.user.contract.application.port.outbound.UserInfoQueryPort
import com.playground.user.contract.domain.vo.UserInfo
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.security.core.userdetails.UsernameNotFoundException

@Suppress("NonAsciiCharacters")
class CustomUserDetailsServiceTest {

    private val userInfoQueryPort: UserInfoQueryPort = mock()

    private val customUserDetailsService = CustomUserDetailsService(userInfoQueryPort)

    @Test
    fun `loginId로 사용자 정보를 조회하여 AuthUserDetails로 반환한다`() {
        val userInfo =
            UserInfo(
                userId = 1L,
                loginId = "tester",
                password = "encoded-password",
                role = "USER",
            )

        given(userInfoQueryPort.getUserInfoByLoginId(userInfo.loginId))
            .willReturn(userInfo)

        val userDetails = customUserDetailsService.loadUserByUsername(userInfo.loginId)

        verify(userInfoQueryPort).getUserInfoByLoginId(userInfo.loginId)

        userDetails.username shouldBe userInfo.loginId
        userDetails.password shouldBe userInfo.password
        userDetails.authorities.shouldHaveSize(1)
        userDetails.authorities.first().authority shouldBe "ROLE_${userInfo.role}"
    }

    @Test
    fun `사용자를 찾지 못하면 예외를 그대로 전달한다`() {
        val loginId = "ghost"
        val expectedException = UsernameNotFoundException("User Not Found with loginId: $loginId")

        given(userInfoQueryPort.getUserInfoByLoginId(loginId))
            .willThrow(expectedException)

        val thrown =
            shouldThrow<UsernameNotFoundException> {
                customUserDetailsService.loadUserByUsername(loginId)
            }

        thrown shouldBe expectedException
        verify(userInfoQueryPort).getUserInfoByLoginId(loginId)
    }
}
