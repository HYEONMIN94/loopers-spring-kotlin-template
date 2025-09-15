package com.loopers.domain.ranking.policy

import com.loopers.domain.ranking.event.RankingEvent
import com.loopers.domain.ranking.type.EventType
import org.springframework.stereotype.Component

@Component
class LikeScorePolicy : ScorePolicy {
    override fun supports(event: RankingEvent) = event.eventType == EventType.LIKE
    override fun calculate(event: RankingEvent) = 0.2
}
