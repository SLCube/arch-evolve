package com.playground.delivery.application.service

import com.playground.delivery.application.converter.toDeliveryAddress
import com.playground.delivery.application.converter.toDeliveryReceiver
import com.playground.delivery.application.port.inbound.DeliveryUsecase
import com.playground.delivery.application.port.inbound.command.DeliveryCreateCommand
import com.playground.delivery.application.port.outbound.DeliveryCommandPort
import com.playground.delivery.application.port.outbound.DeliveryQueryPort
import com.playground.delivery.domain.model.Delivery
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DeliveryService(
    private val deliveryCommandPort: DeliveryCommandPort,
    private val deliveryQueryPort: DeliveryQueryPort,
) : DeliveryUsecase {
    override fun createDelivery(command: DeliveryCreateCommand): Delivery {
        return deliveryQueryPort.findByOrderId(command.orderId)
            .orElseGet {
                val delivery = Delivery.createDelivery(
                    orderId = command.orderId,
                    userId = command.userId,
                    deliveryReceiver = command.toDeliveryReceiver(),
                    deliveryAddress = command.toDeliveryAddress(),
                )

                deliveryCommandPort.save(delivery)
            }
    }
}
