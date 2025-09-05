package com.loopers.infrastructure.event

import java.time.Instant
import java.util.UUID

data class EventEnvelope<T>(
    val eventId: String = UUID.randomUUID().toString(),
    val eventType: String,
    val occurredAt: Instant = Instant.now(),
    val partitionKey: String,
    val payload: T,
) {
    companion object {
        fun <T : Any> of(
            partitionKey: String,
            payload: T,
            eventType: String = payload::class.simpleName ?: "Unknown",
        ): EventEnvelope<T> =
            EventEnvelope(
                eventId = UUID.randomUUID().toString(),
                eventType = eventType,
                occurredAt = Instant.now(),
                partitionKey = partitionKey,
                payload = payload,
            )
    }
}
