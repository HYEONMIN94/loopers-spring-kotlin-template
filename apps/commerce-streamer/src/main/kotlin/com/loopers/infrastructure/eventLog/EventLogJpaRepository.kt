package com.loopers.infrastructure.eventLog

import com.loopers.domain.eventLog.entity.EventLog
import org.springframework.data.jpa.repository.JpaRepository

interface EventLogJpaRepository : JpaRepository<EventLog, Long>
