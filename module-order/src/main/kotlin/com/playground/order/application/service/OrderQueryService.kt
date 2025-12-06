package com.playground.order.application.service

import com.playground.common.application.query.PageQuery
import com.playground.common.application.query.PagedResult
import com.playground.order.application.port.inbound.OrderQueryUseCase
import com.playground.order.application.port.outbound.OrderQueryPort
import com.playground.order.application.provider.OrderExternalDataProvider
import com.playground.order.application.service.result.OrderDetailResult
import com.playground.order.application.service.result.OrderSummaryResult
import com.playground.order.application.validator.OrderOwnerValidator
import com.playground.product.contract.domain.vo.ProductInfo
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class OrderQueryService(
    private val orderQueryPort: OrderQueryPort,
    private val orderExternalDataProvider: OrderExternalDataProvider,
    private val orderOwnerValidator: OrderOwnerValidator,
) : OrderQueryUseCase {
    override fun getOrder(
        userId: Long,
        orderId: Long,
    ): OrderDetailResult {
        orderOwnerValidator.validate(userId, orderId)

        val order = orderQueryPort.findById(orderId)
        val productIds = order.orderProducts.map { it.productId }
        val productInfoMap = orderExternalDataProvider.getVerifiedProductInfos(productIds)

        return OrderDetailResult.of(order, productInfoMap)
    }

    override fun getOrders(
        userId: Long,
        query: PageQuery,
    ): PagedResult<OrderSummaryResult> {
        val pagedOrders = orderQueryPort.findOrdersByUserId(userId, query)

        val allProductIdsInPage = pagedOrders.content.flatMap { it.orderProducts.map { op -> op.productId } }.distinct()

        val productInfoMap: Map<Long, ProductInfo> =
            if (allProductIdsInPage.isNotEmpty()) {
                orderExternalDataProvider.getVerifiedProductInfos(allProductIdsInPage)
            } else {
                emptyMap()
            }

        return pagedOrders.map { order ->
            OrderSummaryResult.of(order, productInfoMap)
        }
    }
}
