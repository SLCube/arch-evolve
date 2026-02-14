package com.playground.order.application.service.result

import com.playground.delivery.contract.domain.vo.DeliveryInfo
import com.playground.order.domain.enum.OrderStatus
import com.playground.order.domain.model.Order
import com.playground.order.domain.model.OrderProduct
import com.playground.product.contract.domain.vo.ProductInfo
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderDetailResult(
    val id: Long,
    val userId: Long,
    val totalPrice: BigDecimal,
    val status: OrderStatus,
    val orderProducts: List<OrderProductDetail>,
    val delivery: OrderDeliveryDetail,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun of(
            order: Order,
            productInfoMap: Map<Long, ProductInfo>,
            deliveryInfo: DeliveryInfo,
        ): OrderDetailResult {
            val orderProductDetails =
                order.orderProducts.map { orderProduct ->
                    val productInfo = productInfoMap.getValue(orderProduct.productId)
                    OrderProductDetail.of(orderProduct, productInfo)
                }

            return OrderDetailResult(
                id = order.id!!,
                userId = order.userId,
                totalPrice = order.totalPrice,
                status = order.status,
                orderProducts = orderProductDetails,
                delivery = OrderDeliveryDetail.of(deliveryInfo),
                createdAt = order.createdAt,
            )
        }
    }
}

data class OrderDeliveryDetail(
    val deliveryId: Long,
    val receiverName: String,
    val receiverPhoneNumber: String,
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
    val deliveryStatus: String,
) {
    companion object {
        fun of(deliveryInfo: DeliveryInfo): OrderDeliveryDetail =
            OrderDeliveryDetail(
                deliveryId = deliveryInfo.deliveryId,
                receiverName = deliveryInfo.receiverName,
                receiverPhoneNumber = deliveryInfo.receiverPhoneNumber,
                zipCode = deliveryInfo.zipCode,
                baseAddress = deliveryInfo.baseAddress,
                detailAddress = deliveryInfo.detailAddress,
                deliveryStatus = deliveryInfo.deliveryStatus,
            )
    }
}

data class OrderProductDetail(
    val id: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val price: BigDecimal,
) {
    companion object {
        fun of(
            orderProduct: OrderProduct,
            productInfo: ProductInfo,
        ): OrderProductDetail =
            OrderProductDetail(
                id = orderProduct.id!!,
                productId = orderProduct.productId,
                productName = productInfo.productName,
                quantity = orderProduct.quantity,
                price = orderProduct.price,
            )
    }
}
