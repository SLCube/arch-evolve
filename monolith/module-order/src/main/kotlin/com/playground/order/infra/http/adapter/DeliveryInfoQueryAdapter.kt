package com.playground.order.infra.http.adapter

import com.playground.common.log.utils.logger
import com.playground.delivery.contract.application.outbound.DeliveryInfoQueryPort
import com.playground.delivery.contract.domain.vo.DeliveryInfo
import com.playground.order.infra.http.dto.DeliveryInfoResponse
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class DeliveryInfoQueryAdapter(
    private val deliveryRestClient: RestClient,
) : DeliveryInfoQueryPort {
    private val log = logger()

    override fun getDeliveryInfoByOrderId(orderId: Long): DeliveryInfo? {
        log.info("delivery-service 배송 정보 조회 [orderId={}]", orderId)
        return try {
            val response =
                deliveryRestClient
                    .get()
                    .uri("/internal/deliveries/{orderId}", orderId)
                    .retrieve()
                    .body(DeliveryInfoResponse::class.java)
                    ?: return null

            DeliveryInfo(
                deliveryId = response.deliveryId,
                orderId = response.orderId,
                userId = response.userId,
                receiverName = response.receiverName,
                receiverPhoneNumber = response.receiverPhoneNumber,
                zipCode = response.zipCode,
                baseAddress = response.baseAddress,
                detailAddress = response.detailAddress,
                deliveryStatus = response.deliveryStatus,
            )
        } catch (e: Exception) {
            log.warn("delivery-service 배송 정보 조회 실패 [orderId={}]: {}", orderId, e.message)
            null
        }
    }
}
