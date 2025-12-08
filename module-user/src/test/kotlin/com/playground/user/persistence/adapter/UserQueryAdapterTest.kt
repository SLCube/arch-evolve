package com.playground.user.persistence.adapter

import com.playground.common.jpa.config.QuerydslConfig
import com.playground.user.domain.enum.UserRole
import com.playground.user.persistence.entity.UserAddressJpaEntity
import com.playground.user.persistence.entity.UserJpaEntity
import com.playground.user.persistence.repository.UserRepository
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@Suppress("NonAsciiCharacters")
@DataJpaTest
@Import(UserQueryAdapter::class, QuerydslConfig::class)
class UserQueryAdapterTest(
    @param:Autowired private val userQueryAdapter: UserQueryAdapter,
    @param:Autowired private val userRepository: UserRepository,
) {

    @Test
    fun `사용자 ID 로 조회하면 Optional 로 도메인 사용자를 반환한다`() {
        // given
        val savedUser = persistUser()

        // when
        val found = userQueryAdapter.findById(savedUser.id!!)

        // then
        found.isPresent.shouldBeTrue()
        found.get().loginId shouldBe savedUser.loginId
        found.get().nickname shouldBe savedUser.nickname
    }

    @Test
    fun `존재하지 않는 로그인 아이디로 조회하면 Optional empty 를 반환한다`() {
        // when
        val found = userQueryAdapter.findByLoginId("unknown-login")

        // then
        found.isEmpty.shouldBeTrue()
    }

    @Test
    fun `닉네임으로 사용자 조회 시 Optional 에 도메인이 담긴다`() {
        // given
        val nickname = "테스트닉네임"
        val savedUser = persistUser(nickname = nickname)

        // when
        val found = userQueryAdapter.findByNickname(nickname)

        // then
        found.isPresent.shouldBeTrue()
        found.get().id shouldBe savedUser.id
    }

    @Test
    fun `주소를 포함하여 사용자 조회 시 Optional 에 사용자가 담긴다`() {
        // given
        val savedUser = persistUser(
            addressEntities = mutableListOf(createAddressEntity())
        )

        // when
        val found = userQueryAdapter.findUserWithAddressById(savedUser.id!!)

        // then
        found.isPresent.shouldBeTrue()
        found.get().id shouldBe savedUser.id
    }

    private fun persistUser(
        loginId: String = "user-login",
        password: String = "user-password",
        nickname: String = "사용자",
        addressEntities: MutableList<UserAddressJpaEntity> = mutableListOf(),
    ): UserJpaEntity {
        val entity = UserJpaEntity(
            loginId = loginId,
            password = password,
            nickname = nickname,
            role = UserRole.USER,
            addressEntities = addressEntities,
        )

        return userRepository.save(entity)
    }

    private fun createAddressEntity(): UserAddressJpaEntity =
        UserAddressJpaEntity(
            id = null,
            userId = 0L,
            receiverName = "수령인",
            receiverPhoneNumber = "01000000000",
            zipCode = "12345",
            baseAddress = "서울시",
            detailAddress = "영등포구 당산동",
            isDefault = true,
        )

}
