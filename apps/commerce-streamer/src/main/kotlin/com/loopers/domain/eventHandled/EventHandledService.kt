package com.loopers.domain.eventHandled

import com.loopers.domain.eventHandled.entity.EventHandled
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class EventHandledService(
    private val eventHandledRepository: EventHandledRepository,
) {
    @Transactional
    fun tryHandle(eventId: String, handler: String): Boolean {
        return try {
            eventHandledRepository.save(EventHandled.create(eventId, handler))
            true
        } catch (e: DataIntegrityViolationException) {
            false
        }
    }
}
