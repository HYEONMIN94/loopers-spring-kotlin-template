package com.loopers.domain.eventHandled.entity

import com.loopers.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant

@Entity
@Table(
    name = "event_handled",
    uniqueConstraints = [UniqueConstraint(columnNames = ["event_id", "handler"])],
)
class EventHandled protected constructor(
    eventId: String,
    handler: String,
) : BaseEntity() {
    @Column(name = "event_id", nullable = false)
    var eventId: String = eventId
        protected set

    @Column(name = "handler", nullable = false)
    var handler: String = handler
        protected set

    @Column(name = "handled_at", nullable = false)
    var handledAt: Instant = Instant.now()
        protected set

    companion object {
        fun create(eventId: String, handler: String): EventHandled {
            return EventHandled(eventId = eventId, handler = handler)
        }
    }
}
