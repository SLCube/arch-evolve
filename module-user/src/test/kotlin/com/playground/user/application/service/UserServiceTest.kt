package com.playground.user.application.service

import com.playground.auth.contract.application.port.outbound.PasswordEncoderPort
import com.playground.user.application.port.outbound.UserCommandPort
import com.playground.user.application.port.outbound.UserEventPort
import com.playground.user.application.port.outbound.UserQueryPort
import com.playground.user.application.validator.UserValidator
import com.playground.user.domain.event.UserNicknameUpdatedEvent
import com.playground.user.domain.event.UserPasswordUpdatedEvent
import com.playground.user.domain.event.UserSignedUpEvent
import com.playground.user.domain.model.User
import com.playground.user.fixture.application.command.UserCommandTestFixture
import com.playground.user.fixture.application.domain.UserDomainTestFixture
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.util.Optional

@Suppress("NonAsciiCharacters")
class UserServiceTest {

    private val userQueryPort: UserQueryPort = mock()
    private val userCommandPort: UserCommandPort = mock()
    private val userEventPort: UserEventPort = mock()
    private val userValidator: UserValidator = mock()
    private val passwordEncoderPort: PasswordEncoderPort = mock()

    private val userService: UserService = UserService(
        userQueryPort = userQueryPort,
        userCommandPort = userCommandPort,
        userEventPort = userEventPort,
        userValidator = userValidator,
        passwordEncoderPort = passwordEncoderPort,
    )

    @Test
    fun `회원 가입 시 중복 검증 이후 사용자 저장 및 가입 이벤트를 발행한다`() {
        // given
        val command = UserCommandTestFixture.signUpCommand(
            loginId = "newUser",
            password = "rawPassword",
            nickname = "새닉네임",
        )
        val encodedPassword = "encoded-pass"
        val savedUser = UserDomainTestFixture.mockUser(
            id = 42L,
            loginId = command.loginId,
            password = encodedPassword,
            nickname = command.nickname,
        )

        given(passwordEncoderPort.encode(command.password))
            .willReturn(encodedPassword)
        given(userCommandPort.save(any<User>()))
            .willReturn(savedUser)

        // when
        val createdUser = userService.signUp(command)

        // then
        verify(userValidator).validateDuplicateLoginId(command.loginId)
        verify(userValidator).validateDuplicateNickname(command.nickname)

        verify(userCommandPort).save(check { saved ->
            saved.loginId shouldBe command.loginId
            saved.nickname shouldBe command.nickname
            saved.password shouldBe encodedPassword
        })

        verify(userEventPort).publish(check<UserSignedUpEvent> { event ->
            event.userId shouldBe savedUser.id
            event.loginId shouldBe savedUser.loginId
        })

        createdUser shouldBe savedUser
    }

    @Test
    fun `닉네임 변경 시 기존 사용자 조회 후 닉네임 업데이트 및 이벤트 발행`() {
        // given
        val userId = 13L
        val oldNickname = "기존"
        val newNickname = "변경"
        val foundUser = UserDomainTestFixture.mockUser(
            id = userId,
            nickname = oldNickname,
        )
        val command = UserCommandTestFixture.updateNicknameCommand(
            userId = userId,
            newNickname = newNickname,
        )

        given(userQueryPort.findById(eq(userId)))
            .willReturn(Optional.of(foundUser))
        given(userCommandPort.update(any<User>()))
            .willReturn(foundUser)

        // when
        val updatedUser = userService.updateNickname(command)

        // then
        verify(userValidator).validateDuplicateNickname(command.newNickname, command.userId)
        verify(userCommandPort).update(check { saved ->
            saved.nickname shouldBe newNickname
        })
        verify(userEventPort).publish(check<UserNicknameUpdatedEvent> { event ->
            event.userId shouldBe userId
            event.loginId shouldBe foundUser.loginId
            event.oldNickname shouldBe oldNickname
            event.newNickname shouldBe newNickname
        })

        updatedUser.nickname shouldBe newNickname
    }

    @Test
    fun `비밀번호 변경 시 기존 비밀번호 검증 후 저장 및 이벤트를 발행한다`() {
        // given
        val userId = 27L
        val oldEncodedPassword = "old-hash"
        val newEncodedPassword = "new-hash"
        val command = UserCommandTestFixture.updatePasswordCommand(
            userId = userId,
            oldPassword = "old-password",
            newPassword = "new-password",
        )
        val foundUser = UserDomainTestFixture.mockUser(
            id = userId,
            password = oldEncodedPassword,
        )

        given(userQueryPort.findById(eq(userId)))
            .willReturn(Optional.of(foundUser))
        given(passwordEncoderPort.encode(command.newPassword))
            .willReturn(newEncodedPassword)
        given(userCommandPort.update(any<User>()))
            .willReturn(foundUser)

        // when
        val updatedUser = userService.updatePassword(command)

        // then
        verify(userValidator).validateOldPassword(command.oldPassword, oldEncodedPassword)
        verify(passwordEncoderPort).encode(command.newPassword)
        verify(userCommandPort).update(check { saved ->
            saved.password shouldBe newEncodedPassword
        })
        verify(userEventPort).publish(check<UserPasswordUpdatedEvent> { event ->
            event.userId shouldBe userId
            event.loginId shouldBe foundUser.loginId
        })

        updatedUser.password shouldBe newEncodedPassword
    }
}
