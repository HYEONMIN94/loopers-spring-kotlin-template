package com.loopers.domain.ranking

import com.loopers.domain.ranking.event.RankingEvent
import com.loopers.domain.ranking.policy.ScorePolicy
import org.springframework.stereotype.Component

@Component
class RankingScoreCalculator(
    private val policies: List<ScorePolicy>,
) {
    fun calculate(event: RankingEvent): Double {
        val policy = policies.find { it.supports(event) }
            ?: throw IllegalArgumentException("이벤트에 대한 정책이 없습니다: ${event.eventType}")
        return policy.calculate(event)
    }
}
