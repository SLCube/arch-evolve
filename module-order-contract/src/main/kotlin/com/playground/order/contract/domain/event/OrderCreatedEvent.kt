package com.playground.order.contract.domain.event

import com.playground.common.event.DomainEvent
import java.math.BigDecimal

/**
 * 새로운 주문이 성공적으로 생성되었음을 알리는 도메인 이벤트.
 *
 * [Order] 도메인의 트랜잭션이 완료된 후 발행되며, 다운스트림 서비스([PaymentService], [DeliveryService] 등)에게
 * 결제 승인, 배송 준비, 재고 차감 등의 후속 처리를 시작할 것을 지시하는 핵심 메시지입니다.
 *
 * 이 이벤트는 동기(Synchronous) 또는 비동기(Asynchronous)로 처리될 수 있으며,
 * **주문 생성 시점의 모든 불변 데이터**를 포함하여 다운스트림의 안정성을 높입니다.
 *
 * @property orderId 새로 생성된 주문의 고유 ID (식별자)
 * @property userId 주문을 요청한 사용자의 ID
 * @property products 주문에 포함된 상품 목록과 수량 (불변 리스트)
 * @property totalAmount 주문의 최종 결제 금액 (BigDecimal로 정합성 보장)
 */
data class OrderCreatedEvent(
    val orderId: Long,
    val userId: Long,
    val products: List<OrderProductDetail>,
    val totalAmount: BigDecimal,
) : DomainEvent {
    data class OrderProductDetail(
        val productId: Long,
        val quantity: Int,
    )
}
