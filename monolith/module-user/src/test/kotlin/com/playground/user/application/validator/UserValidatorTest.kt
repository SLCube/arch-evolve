package com.playground.user.application.validator

import com.playground.auth.contract.application.port.outbound.PasswordEncoderPort
import com.playground.user.application.port.outbound.UserQueryPort
import com.playground.user.domain.exception.DuplicateLoginIdException
import com.playground.user.domain.exception.DuplicateNicknameException
import com.playground.user.domain.exception.PasswordMismatchException
import com.playground.user.fixture.application.domain.UserDomainTestFixture
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import java.util.Optional

@Suppress("NonAsciiCharacters")
class UserValidatorTest {

    private val userQueryPort: UserQueryPort = mock()
    private val passwordEncoderPort: PasswordEncoderPort = mock()

    private val userValidator = UserValidator(
        userQueryPort = userQueryPort,
        passwordEncoderPort = passwordEncoderPort,
    )

    @BeforeEach
    fun setUp() {
        reset(userQueryPort, passwordEncoderPort)
    }

    @Test
    fun `로그인 아이디 중복 시 DuplicateLoginIdException 을 발생시킨다`() {
        // given
        val loginId = "duplicated"
        given(userQueryPort.findByLoginId(loginId))
            .willReturn(Optional.of(UserDomainTestFixture.mockUser(loginId = loginId)))

        // when & then
        assertThrows<DuplicateLoginIdException> {
            userValidator.validateDuplicateLoginId(loginId)
        }
    }

    @Test
    fun `다른 사용자가 사용 중인 닉네임일 경우 DuplicateNicknameException 을 발생시킨다`() {
        // given
        val nickname = "사용중"
        val currentUserId = 5L
        given(userQueryPort.findByNickname(nickname))
            .willReturn(Optional.of(UserDomainTestFixture.mockUser(id = 99L, nickname = nickname)))

        // when & then
        assertThrows<DuplicateNicknameException> {
            userValidator.validateDuplicateNickname(nickname, currentUserId)
        }
    }

    @Test
    fun `현재 사용자와 동일한 닉네임을 사용할 경우 예외를 발생시키지 않는다`() {
        // given
        val nickname = "내닉네임"
        val currentUserId = 7L
        given(userQueryPort.findByNickname(nickname))
            .willReturn(Optional.of(UserDomainTestFixture.mockUser(id = currentUserId, nickname = nickname)))

        // when & then
        assertDoesNotThrow {
            userValidator.validateDuplicateNickname(nickname, currentUserId)
        }
    }

    @Test
    fun `저장된 비밀번호와 일치하지 않을 경우 PasswordMismatchException 을 발생시킨다`() {
        // given
        val oldPassword = "raw-old"
        val storedHash = "encoded"
        given(passwordEncoderPort.matches(oldPassword, storedHash))
            .willReturn(false)

        // when & then
        assertThrows<PasswordMismatchException> {
            userValidator.validateOldPassword(oldPassword, storedHash)
        }
    }

    @Test
    fun `저장된 비밀번호와 일치할 경우 예외를 발생시키지 않는다`() {
        // given
        val oldPassword = "raw-old"
        val storedHash = "encoded"
        given(passwordEncoderPort.matches(oldPassword, storedHash))
            .willReturn(true)

        // when & then
        assertDoesNotThrow {
            userValidator.validateOldPassword(oldPassword, storedHash)
        }
    }
}
