package com.playground.delivery.application.service

import com.playground.delivery.application.converter.toDeliveryAddress
import com.playground.delivery.application.converter.toDeliveryReceiver
import com.playground.delivery.application.factory.from
import com.playground.delivery.application.port.inbound.DeliveryUsecase
import com.playground.delivery.application.port.inbound.command.DeliveryCreateCommand
import com.playground.delivery.application.port.outbound.DeliveryCommandPort
import com.playground.delivery.application.port.outbound.DeliveryQueryPort
import com.playground.delivery.application.port.outbound.DeliveryEventPort
import com.playground.delivery.contract.domain.event.DeliveryCompletedEvent
import com.playground.delivery.contract.domain.event.DeliveryCreatedEvent
import com.playground.delivery.contract.domain.event.DeliveryStartedEvent
import com.playground.delivery.domain.model.Delivery
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DeliveryService(
    private val deliveryCommandPort: DeliveryCommandPort,
    private val deliveryQueryPort: DeliveryQueryPort,
    private val deliveryEventPort: DeliveryEventPort,
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
                val saved = deliveryCommandPort.save(delivery)
                deliveryEventPort.publish(DeliveryCreatedEvent.from(saved))
                saved
            }
    }

    override fun startDelivery(orderId: Long): Delivery {
        val delivery = deliveryQueryPort.findByOrderIdOrThrow(orderId)

        delivery.startDelivery()
        val updated = deliveryCommandPort.update(delivery)
        deliveryEventPort.publish(DeliveryStartedEvent.from(delivery))
        return updated
    }

    override fun completeDelivery(orderId: Long): Delivery {
        val delivery = deliveryQueryPort.findByOrderIdOrThrow(orderId)

        delivery.completeDelivery()
        val updated = deliveryCommandPort.update(delivery)
        deliveryEventPort.publish(DeliveryCompletedEvent.from(delivery))
        return updated
    }
}
