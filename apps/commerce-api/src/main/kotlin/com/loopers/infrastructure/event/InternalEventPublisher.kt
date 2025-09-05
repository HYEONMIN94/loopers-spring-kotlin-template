package com.loopers.infrastructure.event

interface InternalEventPublisher {
    fun <T : Any> publish(topic: String, envelope: EventEnvelope<T>)
}
