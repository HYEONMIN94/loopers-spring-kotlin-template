package com.loopers.application.order.publisher

import com.loopers.infrastructure.event.EventEnvelope
import com.loopers.infrastructure.event.InternalEventPublisher
import com.loopers.infrastructure.event.constans.Topics
import com.loopers.infrastructure.event.dto.ProductSalesChanged
import org.springframework.stereotype.Component

@Component
class OrderEventPublisher(
    private val publisher: InternalEventPublisher,
) {

    fun publishProductSalse(productId: Long, quantity: Long) {
        val payload = ProductSalesChanged(productId, quantity)
        val envelope = EventEnvelope.of(partitionKey = productId.toString(), payload = payload)
        publisher.publish(Topics.PRODUCT_SALSE_EVENTS, envelope)
    }
}
