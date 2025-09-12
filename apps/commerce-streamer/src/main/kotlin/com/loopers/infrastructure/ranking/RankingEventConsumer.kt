package com.loopers.infrastructure.ranking

import com.loopers.domain.ranking.RankingScoreCalculator
import com.loopers.domain.ranking.event.RankingEvent
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class RankingEventConsumer(
    private val keyGenerator: RankingKeyGenerator,
    private val calculator: RankingScoreCalculator,
    private val writer: RankingWriter,
) {
    @KafkaListener(topics = ["ranking-events"])
    fun consume(events: List<RankingEvent>) {
        val key = keyGenerator.keyFor()
        events.forEach { event ->
            val score = calculator.calculate(event)
            writer.write(key, event.productId.toString(), score)
        }
    }
}
