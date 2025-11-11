package com.playground.user.persistence.entity

import com.playground.common.jpa.domain.BaseEntity
import com.playground.user.domain.User
import com.playground.user.domain.enum.UserRole
import jakarta.persistence.*

@Entity
@Table(name = "users")
class UserJpaEntity(
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var loginId: String,
    var password: String,
    var nickname: String,

    @Enumerated(EnumType.STRING)
    var role: UserRole = UserRole.USER,
): BaseEntity() {
    companion object {
        fun toJpaEntity(domain: User): UserJpaEntity {
            return UserJpaEntity(
                id = domain.id,
                loginId = domain.loginId,
                password = domain.password,
                nickname = domain.nickname,
                role = domain.role
            )
        }
    }

    fun update(loginId: String, password: String, nickname: String) {
        this.loginId = loginId
        this.password = password
        this.nickname = nickname
    }
}