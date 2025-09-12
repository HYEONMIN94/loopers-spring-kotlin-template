package com.loopers.application.ranking.publisher

import com.loopers.domain.renking.event.RankingEvent
import com.loopers.domain.renking.type.EventType
import com.loopers.infrastructure.event.EventEnvelope
import com.loopers.infrastructure.event.InternalEventPublisher
import org.springframework.stereotype.Component

@Component
class RankingEventPublisher(
    private val publisher: InternalEventPublisher,
) {
    fun publishViewEvent(productId: Long) {
        val payload = RankingEvent(productId, EventType.VIEW)
        val envelope = EventEnvelope.of(partitionKey = productId.toString(), payload = payload)
        publisher.publish("ranking-events", envelope)
    }

    fun publishLikeEvent(productId: Long) {
        val payload = RankingEvent(productId, EventType.LIKE)
        val envelope = EventEnvelope.of(partitionKey = productId.toString(), payload = payload)
        publisher.publish("ranking-events", envelope)
    }

    fun publishSalesEvent(productId: Long, price: Long, amount: Int) {
        val payload = RankingEvent(productId, EventType.SALES, price, amount)
        val envelope = EventEnvelope.of(partitionKey = productId.toString(), payload = payload)
        publisher.publish("ranking-events", envelope)
    }
}
