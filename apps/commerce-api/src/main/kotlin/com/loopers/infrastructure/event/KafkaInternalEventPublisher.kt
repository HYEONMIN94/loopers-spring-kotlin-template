package com.loopers.infrastructure.event

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Service
class KafkaInternalEventPublisher(
    private val kafkaTemplate: KafkaTemplate<Any, Any>,
) : InternalEventPublisher {

    override fun <T : Any> publish(topic: String, envelope: EventEnvelope<T>) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    kafkaTemplate.send(topic, envelope.partitionKey, envelope)
                }
            })
        } else {
            kafkaTemplate.send(topic, envelope.partitionKey, envelope)
        }
    }
}
