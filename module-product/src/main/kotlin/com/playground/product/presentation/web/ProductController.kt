package com.playground.product.presentation.web

import com.playground.product.application.port.inbound.ProductUseCase
import com.playground.product.application.port.inbound.query.GetProductQuery
import com.playground.product.presentation.mapper.toCommand
import com.playground.product.presentation.request.ProductSaveRequestDto
import com.playground.product.presentation.request.ProductUpdateRequestDto
import com.playground.product.presentation.response.ProductResponseDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products")
class ProductController(
    private val productUseCase: ProductUseCase,
) {
    @PostMapping
    fun save(
        @RequestBody @Valid requestDto: ProductSaveRequestDto,
    ): ResponseEntity<ProductResponseDto> {
        val savedProduct = productUseCase.saveProduct(requestDto.toCommand())
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponseDto.toResponse(savedProduct))
    }

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: Long,
    ): ResponseEntity<ProductResponseDto> {
        val foundProduct = productUseCase.getProduct(GetProductQuery(id))
        return ResponseEntity.ok(ProductResponseDto.toResponse(foundProduct))
    }

    @GetMapping
    fun findAll(): ResponseEntity<List<ProductResponseDto>> {
        val foundProducts = productUseCase.getAllProducts()
        return ResponseEntity.ok(foundProducts.map { ProductResponseDto.toResponse(it) })
    }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody @Valid requestDto: ProductUpdateRequestDto,
    ): ResponseEntity<ProductResponseDto> {
        val updatedProduct = productUseCase.updateProduct(requestDto.toCommand(id))
        return ResponseEntity.ok(ProductResponseDto.toResponse(updatedProduct))
    }
}
