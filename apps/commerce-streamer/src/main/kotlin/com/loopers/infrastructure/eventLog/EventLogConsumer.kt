package com.loopers.infrastructure.eventLog

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.loopers.domain.eventLog.EventLogService
import com.loopers.domain.eventLog.entity.EventLog
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class EventLogConsumer(
    private val eventLogService: EventLogService,
) {
    // TODO: 토픽을 일일히 지정할 수 없으니 AOP를 활용한 공통 로직 변경 필요
    @KafkaListener(
        topicPattern = ".*-events",
        groupId = "audit-log-consumer",
    )
    fun onMessage(record: ConsumerRecord<String, ByteArray>) {
        val root = jacksonObjectMapper().readTree(record.value())

        val eventId = root.get("eventId")?.asText() ?: "unknown"
        val eventType = root.get("eventType")?.asText() ?: "unknown"

        val occurredNode = root.get("occurredAt")
        val occurredAt: Instant = when {
            occurredNode == null || occurredNode.isNull -> Instant.now()
            occurredNode.isNumber -> {
                val d = occurredNode.asDouble()
                val epochMillis = if (d >= 1e12) {
                    d.toLong()
                } else {
                    (d * 1000.0).toLong()
                }
                Instant.ofEpochMilli(epochMillis)
            }

            else -> {
                val text = occurredNode.asText()
                runCatching { Instant.parse(text) }
                    .getOrElse {
                        val num = text.toDoubleOrNull()
                        if (num != null) {
                            val epochMillis = if (num >= 1e12) num.toLong() else (num * 1000.0).toLong()
                            Instant.ofEpochMilli(epochMillis)
                        } else {
                            Instant.now()
                        }
                    }
            }
        }

        val payload = root.get("payload")?.toString() ?: ""

        eventLogService.create(
            EventLog.create(
                eventId = eventId,
                eventType = eventType,
                topic = record.topic(),
                partition = record.partition(),
                offset = record.offset(),
                occurredAt = occurredAt,
                payload = payload,
            ),
        )
    }
}
