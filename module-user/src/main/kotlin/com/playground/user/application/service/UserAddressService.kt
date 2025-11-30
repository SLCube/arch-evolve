package com.playground.user.application.service

import com.playground.user.application.port.inbound.UserAddressUseCase
import com.playground.user.application.port.inbound.command.AddressDefaultSetCommand
import com.playground.user.application.port.inbound.command.AddressRegisterCommand
import com.playground.user.application.port.outbound.UserCommandPort
import com.playground.user.application.port.outbound.UserQueryPort
import com.playground.user.domain.exception.UserNotFoundException
import com.playground.user.domain.model.User
import com.playground.user.domain.model.UserAddress
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserAddressService(
    private val userQueryPort: UserQueryPort,
    private val userCommandPort: UserCommandPort,
) : UserAddressUseCase {
    private fun loadUserAggregate(userId: Long): User {
        val user = userQueryPort.findUserWithAddressById(userId)
            .orElseThrow { UserNotFoundException() }
        return user
    }

    override fun registerAddress(command: AddressRegisterCommand) : UserAddress {
        val user = loadUserAggregate(command.userId)

        val newAddress = UserAddress(
            userId = user.id!!,
            receiverName = command.receiverName,
            receiverPhoneNumber = command.receiverPhoneNumber,
            zipCode = command.zipCode,
            baseAddress = command.baseAddress,
            detailAddress = command.detailAddress,
        )

        user.addAddress(newAddress)

        val updatedUser = userCommandPort.update(user)

        return updatedUser.addresses.last()
    }

    override fun setDefaultAddress(command: AddressDefaultSetCommand) {
        val user = loadUserAggregate(command.userId)

        user.setDefaultAddress(command.addressId)

        userCommandPort.update(user)
    }
}