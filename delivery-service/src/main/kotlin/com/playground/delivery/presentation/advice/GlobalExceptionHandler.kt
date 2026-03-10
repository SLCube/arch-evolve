package com.playground.delivery.presentation.advice

import com.playground.delivery.common.log.utils.logger
import com.playground.delivery.domain.exception.BusinessException
import com.playground.delivery.domain.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = logger()

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        log.warn("BusinessException : {}", e.message)
        val errorCode = e.errorCode
        return ResponseEntity.status(errorCode.httpStatus).body(ErrorResponse(errorCode.code, e.message))
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
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(errorCode.code, errorCode.message(), errors))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnhandledException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unhandled exception occurred", e)
        val errorCode = ErrorCode.INTERNAL_SERVER_ERROR
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse(errorCode.code, errorCode.message()))
    }
}
