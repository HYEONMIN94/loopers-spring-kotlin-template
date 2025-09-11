package com.loopers.domain.eventLog

import com.loopers.domain.eventLog.entity.EventLog

interface EventLogRepository {
    fun save(eventLog: EventLog): EventLog
}
