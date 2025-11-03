package com.playground.common

import com.playground.common.error.ErrorCode
import com.playground.common.error.ErrorResponse
import com.playground.common.utils.logger
import com.playground.product.exception.InsufficientStockException
import com.playground.product.exception.ProductNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = logger()

    @ExceptionHandler(ProductNotFoundException::class)
    fun handleProductNotFoundException(e: ProductNotFoundException): ResponseEntity<ErrorResponse> {
        log.error(e.message)

        val errorCode = ErrorCode.PRODUCT_NOT_FOUND
        val errorMessage = errorCode.message(e.productId)
        val errorResponse = ErrorResponse(errorCode.code, errorMessage)
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = mutableMapOf<String, String?>()
        e.bindingResult.allErrors.forEach { error ->
            if (error is FieldError) {
                errors[error.field] = error.defaultMessage
            }
        }
        log.warn("Validation failed: $errors")

        val errorCode = ErrorCode.INVALID_INPUT
        val errorMessage = errorCode.message()
        val errorResponse = ErrorResponse(errorCode.code, errorMessage, errors)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(InsufficientStockException::class)
    fun handleInsufficientStockException(e: InsufficientStockException): ResponseEntity<ErrorResponse> {
        log.warn(e.message)

        val errorCode = ErrorCode.INSUFFICIENT_STOCK
        val errorResponse = ErrorResponse(errorCode.code, errorCode.message())
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)

    }
}