package com.loopers.domain.ranking.policy

import com.loopers.domain.ranking.event.RankingEvent
import com.loopers.domain.ranking.type.EventType
import org.springframework.stereotype.Component

@Component
class ViewScorePolicy : ScorePolicy {
    override fun supports(event: RankingEvent) = event.eventType == EventType.VIEW
    override fun calculate(event: RankingEvent) = 0.1
}
