package com.playground.product.controller

import com.playground.product.controller.request.ProductSaveRequestDto
import com.playground.product.controller.request.ProductUpdateRequestDto
import com.playground.product.controller.response.ProductResponseDto
import com.playground.product.service.ProductService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService
) {

    @PostMapping
    fun save(@RequestBody @Valid requestDto: ProductSaveRequestDto): ResponseEntity<ProductResponseDto> {
        val savedProduct = productService.save(requestDto.name, requestDto.stock, requestDto.price)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct)
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<ProductResponseDto> {
        val foundProduct = productService.findById(id)
        return ResponseEntity.ok(foundProduct)
    }

    @GetMapping
    fun findAll(): ResponseEntity<List<ProductResponseDto>> {
        val foundProducts = productService.findAll()
        return ResponseEntity.ok(foundProducts)
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody @Valid requestDto: ProductUpdateRequestDto): ResponseEntity<ProductResponseDto> {
        val updatedProduct = productService.update(id, requestDto.name, requestDto.stock, requestDto.price)
        return ResponseEntity.ok(updatedProduct)
    }

    @PostMapping("/{id}/decrease-stock")
    fun decreaseStock(@PathVariable id: Long, @RequestParam quantity: Int): ResponseEntity<ProductResponseDto> {
        val updatedProduct = productService.decreaseStock(id, quantity)
        return ResponseEntity.ok(updatedProduct)
    }
}