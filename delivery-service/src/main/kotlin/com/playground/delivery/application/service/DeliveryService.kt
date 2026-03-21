package com.playground.delivery.application.service

import com.playground.delivery.application.port.inbound.DeliveryUseCase
import com.playground.delivery.application.port.inbound.command.DeliveryCreateCommand
import com.playground.delivery.application.port.outbound.DeliveryCommandPort
import com.playground.delivery.application.port.outbound.DeliveryQueryPort
import com.playground.delivery.application.port.outbound.OutboxCommandPort
import com.playground.delivery.application.support.OutboxFactory
import com.playground.delivery.common.log.utils.logger
import com.playground.delivery.domain.model.Delivery
import com.playground.delivery.domain.model.DeliveryAddress
import com.playground.delivery.domain.model.DeliveryReceiver
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeliveryService(
    private val deliveryCommandPort: DeliveryCommandPort,
    private val deliveryQueryPort: DeliveryQueryPort,
    private val outboxCommandPort: OutboxCommandPort,
    private val outboxFactory: OutboxFactory,
) : DeliveryUseCase {
    private val log = logger()

    @Transactional
    override fun createDelivery(command: DeliveryCreateCommand): Delivery {
        val existing = deliveryQueryPort.findByOrderId(command.orderId).orElse(null)
        if (existing != null) {
            log.info("이미 처리된 배송 [orderId={}, deliveryId={}]", existing.orderId, existing.id)
            return existing
        }

        val delivery =
            Delivery.createDelivery(
                orderId = command.orderId,
                userId = command.userId,
                deliveryReceiver =
                    DeliveryReceiver(
                        receiverName = command.receiverName,
                        receiverPhoneNumber = command.receiverPhoneNumber,
                    ),
                deliveryAddress =
                    DeliveryAddress(
                        zipCode = command.zipCode,
                        baseAddress = command.baseAddress,
                        detailAddress = command.detailAddress,
                    ),
            )

        val saved = deliveryCommandPort.save(delivery)
        outboxCommandPort.save(outboxFactory.deliveryCreated(saved))
        log.info("배송 생성 완료 [orderId={}, deliveryId={}]", saved.orderId, saved.id)
        return saved
    }
}
