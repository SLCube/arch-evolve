package com.playground.user.domain.model

import com.playground.user.domain.enum.UserRole
import com.playground.user.domain.exception.AddressLimitExceededException
import com.playground.user.domain.exception.AddressNotFoundException
import com.playground.user.domain.exception.SameNicknameException

class User(
    val id: Long? = null,
    val loginId: String,
    var password: String,
    var nickname: String,
    val role: UserRole = UserRole.USER,
    val addresses: MutableList<UserAddress> = mutableListOf()
) {
    fun updateNickname(newNickname: String) {
        if (this.nickname == newNickname) {
            throw SameNicknameException()
        }
        this.nickname = newNickname
    }

    fun updatePassword(encodedNewPassword: String) {
        this.password = encodedNewPassword
    }

    private val maxAddressCount = 4

    fun addAddress(address: UserAddress) {
        if (addresses.size >= maxAddressCount) {
            throw AddressLimitExceededException(maxAddressCount)
        }
        if (addresses.isEmpty()) {
            address.setDefault()
        }

        addresses.add(address)
    }

    fun setDefaultAddress(addressId: Long) {
        val targetAddress = addresses.find { it.id == addressId }
            ?: throw AddressNotFoundException(this.id!!, addressId)

        if (targetAddress.isDefault) {
            return
        }

        addresses.find { it.isDefault }?.unsetDefault()

        targetAddress.setDefault()
    }

    override fun toString(): String =
        "User(id=$id, loginId='$loginId', password='****', nickname='$nickname', role=$role)"
}
