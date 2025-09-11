package com.loopers.application.product.publisher

import com.loopers.infrastructure.event.EventEnvelope
import com.loopers.infrastructure.event.InternalEventPublisher
import com.loopers.infrastructure.event.constans.Topics
import com.loopers.infrastructure.event.dto.ProductViewed
import org.springframework.stereotype.Component

@Component
class ProductEventPublisher(
    private val publisher: InternalEventPublisher,
) {
    fun publishProductViewed(productId: Long, delta: Long) {
        val payload = ProductViewed(productId, delta)
        val envelope = EventEnvelope.of(partitionKey = productId.toString(), payload = payload)
        publisher.publish(Topics.PRODUCT_VIEW_EVENTS, envelope)
    }
}
