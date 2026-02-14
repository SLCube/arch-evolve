package com.playground.user.persistence.adapter

import com.playground.common.jpa.config.QuerydslConfig
import com.playground.user.contract.domain.vo.UserInfo
import com.playground.user.domain.enum.UserRole
import com.playground.user.persistence.entity.UserJpaEntity
import com.playground.user.persistence.repository.UserRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.security.core.userdetails.UsernameNotFoundException

@Suppress("NonAsciiCharacters")
@DataJpaTest
@Import(UserInfoQueryAdapter::class, QuerydslConfig::class)
class UserInfoQueryAdapterTest(
    @param:Autowired private val userInfoQueryAdapter: UserInfoQueryAdapter,
    @param:Autowired private val userRepository: UserRepository,
) {

    @Test
    fun `로그인 아이디로 사용자 정보를 조회하면 UserInfo 를 반환한다`() {
        // given
        val loginId = "login-id"
        val password = "encoded-password"
        val savedUser = userRepository.save(
            UserJpaEntity(
                loginId = loginId,
                password = password,
                nickname = "닉네임",
                role = UserRole.ADMIN,
            )
        )

        // when
        val userInfo: UserInfo = userInfoQueryAdapter.getUserInfoByLoginId(loginId)

        // then
        userInfo.userId shouldBe savedUser.id
        userInfo.loginId shouldBe savedUser.loginId
        userInfo.password shouldBe password
        userInfo.role shouldBe UserRole.ADMIN.name
    }

    @Test
    fun `존재하지 않는 로그인 아이디로 조회 시 UsernameNotFoundException 을 던진다`() {
        // when & then
        shouldThrow<UsernameNotFoundException> {
            userInfoQueryAdapter.getUserInfoByLoginId("missing-login")
        }
    }
}
