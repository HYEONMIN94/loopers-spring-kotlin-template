package com.loopers.application.like.publisher

import com.loopers.infrastructure.event.EventEnvelope
import com.loopers.infrastructure.event.InternalEventPublisher
import com.loopers.infrastructure.event.constans.Topics
import com.loopers.infrastructure.event.dto.ProductLikeChanged
import org.springframework.stereotype.Component

@Component
class LikeEventPublisher(
    private val publisher: InternalEventPublisher,
) {
    fun publishLike(productId: Long, delta: Int) {
        val payload = ProductLikeChanged(productId, delta)
        val envelope = EventEnvelope.of(partitionKey = productId.toString(), payload = payload, eventType = "ProductLike")
        publisher.publish(Topics.LIKE_EVENTS, envelope)
    }

    fun publishUnLike(productId: Long, delta: Int) {
        val payload = ProductLikeChanged(productId, delta)
        val envelope = EventEnvelope.of(partitionKey = productId.toString(), payload = payload, eventType = "ProductUnLike")
        publisher.publish(Topics.LIKE_EVENTS, envelope)
    }
}
