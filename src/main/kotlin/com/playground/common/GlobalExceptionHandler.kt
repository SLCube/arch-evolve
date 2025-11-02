package com.playground.common

import com.playground.common.error.ErrorResponse
import com.playground.common.utils.logger
import com.playground.product.exception.ProductNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = logger()

    @ExceptionHandler(ProductNotFoundException::class)
    fun handleProductNotFoundException(e: ProductNotFoundException): ResponseEntity<ErrorResponse> {
        log.error(e.message)

        val errorResponse = ErrorResponse(e.message ?: "상품을 찾을 수 없습니다.")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }
}