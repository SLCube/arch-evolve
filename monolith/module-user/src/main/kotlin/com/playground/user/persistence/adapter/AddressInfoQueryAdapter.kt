package com.playground.user.persistence.adapter

import com.playground.user.contract.application.port.outbound.AddressInfoQueryPort
import com.playground.user.contract.domain.vo.ReceiverAddressInfo
import com.playground.user.domain.exception.AddressNotFoundException
import com.playground.user.persistence.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class AddressInfoQueryAdapter(
    private val userRepository: UserRepository,
): AddressInfoQueryPort {
    override fun getAddressInfoByAddressId(
        userId: Long,
        addressId: Long
    ): ReceiverAddressInfo {
        val user = userRepository.findWithAddressById(userId)
            .orElseThrow { AddressNotFoundException(userId, addressId) }

        val address = user.addressEntities.firstOrNull { it.id == addressId }
            ?: throw AddressNotFoundException(userId, addressId)

        return ReceiverAddressInfo(
            zipCode = address.zipCode,
            baseAddress = address.baseAddress,
            detailAddress = address.detailAddress,
            receiverName = address.receiverName,
            receiverPhoneNumber = address.receiverPhoneNumber,
        )
    }
}
