package com.playground.product.service

import com.playground.product.controller.response.ProductResponseDto
import com.playground.product.domain.Product
import com.playground.product.exception.ProductNotFoundException
import com.playground.product.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository
) {
    fun save(name: String, stock: Int): ProductResponseDto {
        val product = Product(name = name, stock = stock)
        val savedProduct = productRepository.save(product)
        return ProductResponseDto.toResponse(savedProduct)
    }

    fun findById(id: Long): ProductResponseDto {
        val product = productRepository.findById(id).orElseThrow { ProductNotFoundException(id) }
        return ProductResponseDto.toResponse(product)
    }

    fun findAll(): List<ProductResponseDto> = productRepository.findAll()
        .map { ProductResponseDto.toResponse(it) }

    fun update(id: Long, name: String, stock: Int): ProductResponseDto {
        val foundProduct = productRepository.findById(id).orElseThrow { ProductNotFoundException(id) }
        foundProduct.update(name, stock)

        return ProductResponseDto.toResponse(foundProduct)
    }

    fun decreaseStock(id: Long, quantity: Int): ProductResponseDto {
        val foundProduct = productRepository.findByIdWithPerssimisticLock(id).orElseThrow { ProductNotFoundException(id) }
        foundProduct.decreaseStock(quantity)

        return ProductResponseDto.toResponse(foundProduct)
    }
}