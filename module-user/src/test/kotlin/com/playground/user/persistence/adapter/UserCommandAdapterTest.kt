package com.playground.user.persistence.adapter

import com.playground.common.jpa.config.QuerydslConfig
import com.playground.user.domain.exception.UserNotFoundException
import com.playground.user.fixture.application.domain.UserDomainTestFixture
import com.playground.user.persistence.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@Suppress("NonAsciiCharacters")
@DataJpaTest
@Import(UserCommandAdapter::class, QuerydslConfig::class)
class UserCommandAdapterTest(
    @param:Autowired private val userCommandAdapter: UserCommandAdapter,
    @param:Autowired private val userRepository: UserRepository,
) {

    @Test
    fun `save 호출 시 User 도메인 모델을 DB 에 저장하고 ID 가 채번된 객체를 반환한다`() {
        // given
        val unsavedUser = UserDomainTestFixture.mockUser(
            id = null,
            loginId = "user-save",
            password = "raw-pass",
            nickname = "저장유저",
        )

        // when
        val savedUser = userCommandAdapter.save(unsavedUser)

        // then
        savedUser.id shouldNotBe null
        savedUser.loginId shouldBe unsavedUser.loginId
        savedUser.nickname shouldBe unsavedUser.nickname

        val foundEntity = userRepository.findById(savedUser.id!!).orElse(null)
        foundEntity shouldNotBe null
        foundEntity!!.loginId shouldBe unsavedUser.loginId
        foundEntity.nickname shouldBe unsavedUser.nickname
        foundEntity.password shouldBe unsavedUser.password
    }

    @Test
    fun `update 호출 시 User 도메인 모델의 변경 사항을 저장한다`() {
        // given
        val savedUser = userCommandAdapter.save(
            UserDomainTestFixture.mockUser(
                id = null,
                loginId = "user-update",
                password = "old-pass",
                nickname = "기존닉",
            )
        )

        val newNickname = "새닉네임"
        val newPassword = "encoded-new"
        savedUser.updateNickname(newNickname)
        savedUser.updatePassword(newPassword)

        // when
        val updatedUser = userCommandAdapter.update(savedUser)

        // then
        updatedUser.nickname shouldBe newNickname
        updatedUser.password shouldBe newPassword

        val foundEntity = userRepository.findById(savedUser.id!!).orElseThrow()
        foundEntity.nickname shouldBe newNickname
        foundEntity.password shouldBe newPassword
    }

    @Test
    fun `존재하지 않는 사용자 업데이트 시 UserNotFoundException 을 던진다`() {
        // given
        val nonExisting = UserDomainTestFixture.mockUser(
            id = 999L,
            loginId = "ghost",
            password = "pass",
            nickname = "유령",
        )

        // when & then
        shouldThrow<UserNotFoundException> {
            userCommandAdapter.update(nonExisting)
        }
    }
}
