package com.loopers.domain.ranking.policy

import com.loopers.domain.ranking.event.RankingEvent
import com.loopers.domain.ranking.type.EventType
import org.springframework.stereotype.Component
import kotlin.math.log10

@Component
class OrderScorePolicy : ScorePolicy {
    override fun supports(event: RankingEvent) = event.eventType == EventType.SALES
    override fun calculate(event: RankingEvent): Double {
        val price = event.price ?: 0
        val amount = event.amount ?: 0
        return 0.6 * log10((price * amount).toDouble() + 1)
    }
}
