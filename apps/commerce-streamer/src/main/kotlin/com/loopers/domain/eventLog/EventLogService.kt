package com.loopers.domain.eventLog

import com.loopers.domain.eventLog.entity.EventLog
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class EventLogService(
    private val eventLogRepository: EventLogRepository,
) {
    @Transactional
    fun create(eventLog: EventLog) {
        eventLogRepository.save(eventLog)
    }
}
