package com.playground.order.domain.model

import com.playground.user.contract.domain.vo.ReceiverAddressInfo

data class OrderReceiver(
    val receiverName: String,
    val receiverPhoneNumber: String,
) {
    companion object {
        fun fromAddressInfo(receiverAddressInfo: ReceiverAddressInfo): OrderReceiver {
            return OrderReceiver(
                receiverName = receiverAddressInfo.receiverName,
                receiverPhoneNumber = receiverAddressInfo.receiverPhoneNumber,
            )
        }
    }
}
