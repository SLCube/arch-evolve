package com.playground.order.application.support

import com.playground.order.application.factory.from
import com.playground.order.application.port.outbound.OrderCommandPort
import com.playground.order.application.port.outbound.OrderEventPort
import com.playground.order.application.port.outbound.OutboxCommandPort
import com.playground.order.application.provider.OrderExternalDataProvider
import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.order.domain.model.Order
import com.playground.product.contract.domain.vo.ProductInfo
import com.playground.user.contract.domain.vo.ReceiverAddressInfo
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 주문 생성 흐름에서 트랜잭션 경계를 관리하는 컴포넌트.
 *
 * [OrderCommandService.createOrder]는 외부 데이터 조회(Product, Address) 이후
 * DB INSERT + Redis 재고 차감을 수행한다.
 * 클래스 레벨 @Transactional을 그대로 두면 readOnly 조회 구간에도 DB 커넥션을 점유하므로,
 * 이 클래스를 통해 트랜잭션을 메서드 단위로 분리한다.
 *
 * - [getExternalData]: readOnly TX → SELECT 완료 후 즉시 커넥션 반환
 * - [saveOrderWithEventAndOutbox]: write TX → INSERT × 2 + Redis 재고 차감을 하나의 TX로 보장
 *
 * @see OrderCommandService 실제 주문 생성 흐름 오케스트레이션
 */
@Component
class OrderTransactionManager(
    private val orderCommandPort: OrderCommandPort,
    private val outboxCommandPort: OutboxCommandPort,
    private val orderEventPort: OrderEventPort,
    private val outboxFactory: OutboxFactory,
    private val orderExternalDataProvider: OrderExternalDataProvider,
) {
    fun getExternalData(
        productIds: List<Long>,
        userId: Long,
        addressId: Long,
    ): Pair<Map<Long, ProductInfo>, ReceiverAddressInfo> {
        val productInfoMap = orderExternalDataProvider.getVerifiedProductInfos(productIds)
        val addressInfo = orderExternalDataProvider.getAddressInfoByAddressId(userId, addressId)
        return productInfoMap to addressInfo
    }

    @Transactional
    fun saveOrderWithEventAndOutbox(order: Order): Order {
        val savedOrder = orderCommandPort.save(order)
        val event = OrderCreatedEvent.from(savedOrder)
        orderEventPort.publish(event)
        outboxCommandPort.save(outboxFactory.from(event))
        return savedOrder
    }
}
