package com.loopers.infrastructure.event.dto

data class ProductSalesChanged(
    val productId: Long,
    val quantity: Long,
)
