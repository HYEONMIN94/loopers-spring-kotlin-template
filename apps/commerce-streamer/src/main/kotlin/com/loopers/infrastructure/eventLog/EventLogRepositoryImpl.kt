package com.loopers.infrastructure.eventLog

import com.loopers.domain.eventLog.EventLogRepository
import com.loopers.domain.eventLog.entity.EventLog
import org.springframework.stereotype.Component

@Component
class EventLogRepositoryImpl(
    private val eventLogRepository: EventLogJpaRepository,
) : EventLogRepository {
    override fun save(eventLog: EventLog): EventLog {
        return eventLogRepository.save(eventLog)
    }
}
