package com.loopers.infrastructure.eventHandled

import com.loopers.domain.eventHandled.entity.EventHandled
import org.springframework.data.jpa.repository.JpaRepository

interface EventHandledJpaRepository : JpaRepository<EventHandled, Long> {
    fun existsByEventIdAndHandler(eventId: String, handler: String): Boolean
}
