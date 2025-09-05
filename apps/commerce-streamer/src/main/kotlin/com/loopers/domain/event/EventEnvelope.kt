package com.loopers.domain.event

import java.time.Instant
import java.util.UUID

data class EventEnvelope<T>(
    val eventId: String = UUID.randomUUID().toString(),
    val eventType: String,
    val occurredAt: Instant = Instant.now(),
    val partitionKey: String,
    val payload: T,
)
