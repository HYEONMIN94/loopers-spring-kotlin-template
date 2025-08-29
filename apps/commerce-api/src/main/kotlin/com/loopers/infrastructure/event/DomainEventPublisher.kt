package com.loopers.infrastructure.event

interface DomainEventPublisher {
    fun publish(event: Any)
}
