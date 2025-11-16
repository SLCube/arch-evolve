package com.playground.order.presentation.web

import com.playground.auth.application.security.AuthUserDetails
import com.playground.common.application.query.PageQuery
import com.playground.common.presentation.response.PagedResponse
import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.application.port.inbound.OrderQueryUseCase
import com.playground.order.application.port.inbound.command.OrderCancelCommand
import com.playground.order.presentation.mapper.toCommand
import com.playground.order.presentation.request.OrderCreateRequestDto
import com.playground.order.presentation.response.OrderDetailResponseDto
import com.playground.order.presentation.response.OrderResponseDto
import com.playground.order.presentation.response.OrderSummaryResponseDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderCommandUseCase: OrderCommandUseCase,
    private val orderQueryUseCase: OrderQueryUseCase,
) {
    @PostMapping
    fun createOrder(
        @AuthenticationPrincipal authUserDetails: AuthUserDetails,
        @RequestBody @Valid request: OrderCreateRequestDto,
    ): ResponseEntity<OrderResponseDto> {
        val command = request.toCommand(authUserDetails.getUserId())
        val createdOrder = orderCommandUseCase.createOrder(command)

        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponseDto.toResponse(createdOrder))
    }

    @PatchMapping("/{orderId}/cancel")
    fun cancelOrder(
        @AuthenticationPrincipal authUserDetails: AuthUserDetails,
        @PathVariable orderId: Long,
    ): ResponseEntity<OrderResponseDto> {
        val command = OrderCancelCommand(userId = authUserDetails.getUserId(), orderId = orderId)
        val cancelledOrder = orderCommandUseCase.cancelOrder(command)

        return ResponseEntity.status(HttpStatus.OK).body(OrderResponseDto.toResponse(cancelledOrder))
    }

    @GetMapping("/{orderId}")
    fun getOrder(
        @AuthenticationPrincipal authUserDetails: AuthUserDetails,
        @PathVariable orderId: Long,
    ): ResponseEntity<OrderDetailResponseDto> {
        val orderDetailResult = orderQueryUseCase.getOrder(authUserDetails.getUserId(), orderId)
        val response = OrderDetailResponseDto.toResponse(orderDetailResult)

        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @GetMapping
    fun getOrders(
        @AuthenticationPrincipal authUserDetails: AuthUserDetails,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) sortBy: String?,
        @RequestParam(required = false) direction: String?,
    ): ResponseEntity<PagedResponse<OrderSummaryResponseDto>> {
        val pageQuery =
            PageQuery(
                pageNumber = page,
                pageSize = size,
                sortBy = sortBy,
                direction = direction,
            )

        val pagedOrderSummaries = orderQueryUseCase.getOrders(authUserDetails.getUserId(), pageQuery)
        val response = PagedResponse.of(pagedOrderSummaries) { OrderSummaryResponseDto.of(it) }

        return ResponseEntity.status(HttpStatus.OK).body(response)
    }
}
