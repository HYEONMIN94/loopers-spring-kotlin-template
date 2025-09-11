package com.loopers.infrastructure.eventHandled

import com.loopers.domain.eventHandled.EventHandledRepository
import com.loopers.domain.eventHandled.entity.EventHandled
import org.springframework.stereotype.Component

@Component
class EventHandledRepositoryImpl(
    private val eventHandledRepository: EventHandledJpaRepository,
) : EventHandledRepository {
    override fun existsByEventIdAndHandler(eventId: String, handler: String): Boolean {
        return eventHandledRepository.existsByEventIdAndHandler(eventId, handler)
    }

    override fun save(eventHandled: EventHandled): EventHandled {
        return eventHandledRepository.save(eventHandled)
    }
}
