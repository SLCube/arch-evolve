package com.playground.user.persistence.entity

import com.playground.common.persistence.jpa.BaseEntity
import com.playground.user.domain.enum.UserRole
import com.playground.user.domain.model.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

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

    @OneToMany(
        cascade = [CascadeType.PERSIST, CascadeType.MERGE],
        orphanRemoval = true, 
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "user_id")
    val addressEntities: MutableList<UserAddressJpaEntity> = mutableListOf(),
) : BaseEntity() {
    companion object {
        fun toJpaEntity(domain: User): UserJpaEntity =
            UserJpaEntity(
                id = domain.id,
                loginId = domain.loginId,
                password = domain.password,
                nickname = domain.nickname,
                role = domain.role,
            )
    }

    fun update(
        loginId: String,
        password: String,
        nickname: String,
    ) {
        this.loginId = loginId
        this.password = password
        this.nickname = nickname
    }
}
