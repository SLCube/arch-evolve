package com.playground.user.persistence.entity

import com.playground.common.jpa.domain.BaseEntity
import com.playground.user.domain.enum.UserRole
import com.playground.user.domain.exception.SameNicknameException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User(
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

    fun updateNickname(newNickname: String) {
        if (this.nickname == newNickname) {
            throw SameNicknameException()
        }
        this.nickname = newNickname
    }

    fun updatePassword(encodedNewPassword: String) {
        this.password = encodedNewPassword
    }
}