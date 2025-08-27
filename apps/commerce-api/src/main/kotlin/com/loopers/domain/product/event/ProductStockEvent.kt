package com.loopers.domain.product.event

import com.loopers.domain.product.dto.command.ProductStockCommand

class ProductStockEvent {
    data class DecreaseStocksEvent(
        val decreaseStockEvents: List<DecreaseStockEvent>,
    ) {
        fun toCommand(): ProductStockCommand.GetDecreaseStock {
            return ProductStockCommand.GetDecreaseStock(
                decreaseStockEvents.map {
                    ProductStockCommand.GetDecreaseStock.DecreaseStock(it.productOptionId, it.quantity)
                },
            )
        }
    }

    data class DecreaseStockEvent(
        val productOptionId: Long,
        val quantity: Int,
    )
}
