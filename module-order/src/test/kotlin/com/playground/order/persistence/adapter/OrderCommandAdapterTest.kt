package com.playground.order.persistence.adapter

import com.playground.order.domain.enum.OrderStatus
import com.playground.order.domain.exception.OrderNotFoundException
import com.playground.order.fixture.OrderTestFixture
import com.playground.order.persistence.repository.OrderProductRepository
import com.playground.order.persistence.repository.OrderRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@Suppress("NonAsciiCharacters")
@DataJpaTest
@Import(OrderCommandAdapter::class)
class OrderCommandAdapterTest(
    @param:Autowired private val orderCommandAdapter: OrderCommandAdapter,
    @param:Autowired private val orderRepository: OrderRepository,
    @param:Autowired private val orderProductRepository: OrderProductRepository,
) {

    private val initialUserId = 10L

    @Test
    fun `save 호출 시 Order 도메인 모델을 DB에 저장하고 ID가 할당된 객체를 반환해야 한다`() {
        // Given
        val unsavedProducts = OrderTestFixture.mockUnsavedOrderProducts()
        val unsavedOrder = OrderTestFixture.mockOrder(
            id = null,
            userId = initialUserId,
            orderProducts = unsavedProducts
        )

        // When
        val savedOrder = orderCommandAdapter.save(unsavedOrder)

        // Then
        savedOrder.id shouldNotBe null
        savedOrder.userId shouldBe initialUserId

        val foundEntity = orderRepository.findById(savedOrder.id!!).orElse(null)
        foundEntity shouldNotBe null
        foundEntity.status shouldBe OrderStatus.PENDING

        val foundProductEntities = orderProductRepository.findByOrderId(savedOrder.id)
        foundProductEntities.size shouldBe savedOrder.orderProducts.size

        val originalFirstProduct = unsavedProducts.first()
        val savedFirstProduct = foundProductEntities.first()

        savedFirstProduct shouldNotBe null
        savedFirstProduct.productId shouldBe originalFirstProduct.productId
        savedFirstProduct.quantity shouldBe originalFirstProduct.quantity
        savedFirstProduct.price shouldBe originalFirstProduct.price
    }

    @Test
    fun `update 호출 시 Order 도메인 모델의 변경 사항이 DB에 반영되어야 한다`() {
        // Given
        val initialOrder = orderCommandAdapter.save(
            OrderTestFixture.mockOrder(
                id = null,
                userId = initialUserId,
                orderProducts = OrderTestFixture.mockUnsavedOrderProducts(),
            )
        )

        val orderId = initialOrder.id!!

        val newPgTxId = "TX_COMPLETED_123"
        initialOrder.completeOrder(newPgTxId)

        // When
        orderCommandAdapter.update(initialOrder)

        // Then
        val updatedOrder = orderRepository.findById(orderId).get()
        updatedOrder.status shouldBe OrderStatus.COMPLETED
        updatedOrder.pgTransactionId shouldBe newPgTxId
    }

    @Test
    fun `존재하지 않는 주문에 대한 update 호출 시 OrderNotFoundException을 던져야 한다`() {
        // given
        val nonExistingOrderId = 999L
        val mockOrder = OrderTestFixture.mockOrder(
            id = nonExistingOrderId,
            userId = initialUserId
        )

        // when & then
        shouldThrow<OrderNotFoundException> {
            orderCommandAdapter.update(mockOrder)
        }
    }
}