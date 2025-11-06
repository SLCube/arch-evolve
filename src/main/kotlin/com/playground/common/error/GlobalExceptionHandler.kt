package com.playground.common.error

import com.playground.common.utils.logger
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
        val errorResponse = ErrorResponse(errorCode.code, e.message)

        return ResponseEntity.status(errorCode.httpStatus).body(errorResponse)
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
}