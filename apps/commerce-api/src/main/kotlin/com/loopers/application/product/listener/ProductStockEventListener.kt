package com.loopers.application.product.listener

import com.loopers.domain.product.ProductStockService
import com.loopers.domain.product.event.ProductStockEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ProductStockEventListener(
    private val productStockService: ProductStockService,
) {
    @EventListener
    fun processFailure(event: ProductStockEvent.DecreaseStocksEvent) {
        val decreaseStocks = productStockService.getDecreaseStock(event.toCommand())
        productStockService.decreaseStocks(decreaseStocks.toCommand())
    }
}
