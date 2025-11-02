package com.playground.product.exception

class ProductNotFoundException(val productId: Long) : RuntimeException()