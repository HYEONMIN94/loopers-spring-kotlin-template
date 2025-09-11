package com.loopers.domain.eventHandled

import com.loopers.domain.eventHandled.entity.EventHandled

interface EventHandledRepository {
    fun existsByEventIdAndHandler(eventId: String, handler: String): Boolean

    fun save(eventHandled: EventHandled): EventHandled
}
