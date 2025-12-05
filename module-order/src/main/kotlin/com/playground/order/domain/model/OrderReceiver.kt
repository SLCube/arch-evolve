package com.playground.order.domain.model

import com.playground.user.contract.domain.vo.ReceiverAddressInfo

data class OrderReceiver(
    val receiverName: String,
    val reveiverPhoneNumber: String,
) {
    companion object {
        fun fromAddressInfo(receiverAddressInfo: ReceiverAddressInfo): OrderReceiver {
            return OrderReceiver(
                receiverName = receiverAddressInfo.receiverName,
                reveiverPhoneNumber = receiverAddressInfo.receiverPhoneNumber,
            )
        }
    }
}
