package com.playground.order.infra.event.adapter

import com.playground.common.event.DomainEvent
import com.playground.order.application.port.outbound.OrderEventPort
import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.order.fixture.application.domain.OrderDomainTestFixture
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.context.ApplicationEventPublisher

@Suppress("NonAsciiCharacters")
class OrderEventAdapterTest {

    private val eventPublisher: ApplicationEventPublisher = mock()

    private val orderEventPort: OrderEventPort = OrderEventAdapter(eventPublisher)

    @Test
    fun `publish 호출 시 ApplicationEventPublisher의 publishEvent가 정확한 Event 객체로 호출되어야 한다`() {
        // Given
        val mockOrder = OrderDomainTestFixture.mockOrder(id = 100L)
        val orderProductDetails = mockOrder.orderProducts.map {
            OrderCreatedEvent.OrderProductDetail(
                productId = it.productId,
                quantity = it.quantity
            )
        }
        val event: DomainEvent = OrderCreatedEvent(
            orderId = mockOrder.id!!,
            userId = mockOrder.userId,
            products = orderProductDetails,
            totalAmount = mockOrder.totalPrice
        )

        // When
        orderEventPort.publish(event)

        // Then
        verify(eventPublisher).publishEvent(eq(event))
    }
}