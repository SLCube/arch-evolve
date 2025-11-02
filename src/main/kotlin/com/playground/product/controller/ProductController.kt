package com.playground.product.controller

import com.playground.product.controller.request.ProductSaveRequestDto
import com.playground.product.controller.request.ProductUpdateRequestDto
import com.playground.product.domain.Product
import com.playground.product.service.ProductService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/products")
class ProductController(
    private val productService: ProductService
) {

    @PostMapping
    fun save(@RequestBody requestDto: ProductSaveRequestDto): ResponseEntity<Product> {
        val savedProduct = productService.save(requestDto.name, requestDto.stock)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct)
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Product> {
        val foundProduct = productService.findById(id)
        return ResponseEntity.status(HttpStatus.OK).body(foundProduct)
    }

    @GetMapping
    fun findAll(): ResponseEntity<List<Product>> {
        val foundProducts = productService.findAll()
        return ResponseEntity.status(HttpStatus.OK).body(foundProducts)
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody requestDto: ProductUpdateRequestDto): ResponseEntity<Product> {
        val updatedProduct = productService.update(id, requestDto.name, requestDto.stock)
        return ResponseEntity.status(HttpStatus.OK).body(updatedProduct)
    }
}