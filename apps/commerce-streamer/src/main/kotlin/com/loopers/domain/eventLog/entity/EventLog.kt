package com.loopers.domain.eventLog.entity

import com.loopers.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Lob
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "event_log",
    uniqueConstraints = [UniqueConstraint(columnNames = ["event_id"])],
)
class EventLog protected constructor(
    eventId: String,
    eventType: String,
    topic: String,
    partition: Int,
    offset: Long,
    occurredAt: Instant,
    payload: String,
) : BaseEntity() {
    @Column(name = "event_id", nullable = false)
    var eventId: String = eventId
    protected set

    @Column(name = "event_type", nullable = false)
    var eventType: String = eventType
    protected set

    @Column(name = "topic", nullable = false)
    var topic: String = topic
    protected set

    @Column(name = "partition_no", nullable = false)
    var partition: Int = partition
    protected set

    @Column(name = "offset_no", nullable = false)
    var offset: Long = offset
    protected set

    @Column(name = "occurred_at", nullable = false)
    var occurredAt: Instant = occurredAt
    protected set

    @Lob
    @Column(name = "payload", nullable = false)
    var payload: String = payload
    protected set

    @Column(name = "received_at", nullable = false)
    var receivedAt: Instant = Instant.now()
    protected set

    companion object {
        fun create(
            eventId: String,
            eventType: String,
            topic: String,
            partition: Int,
            offset: Long,
            occurredAt: Instant,
            payload: String,
        ) = EventLog(
            eventId = eventId,
            eventType = eventType,
            topic = topic,
            partition = partition,
            offset = offset,
            occurredAt = occurredAt,
            payload = payload,
        )
    }
}
