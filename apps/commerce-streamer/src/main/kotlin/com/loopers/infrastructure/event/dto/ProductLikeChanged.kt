package com.loopers.infrastructure.event.dto

data class ProductLikeChanged(
    val productId: Long,
    val delta: Long,
)
