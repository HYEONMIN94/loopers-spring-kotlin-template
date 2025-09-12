package com.loopers.domain.ranking.policy

import com.loopers.domain.ranking.event.RankingEvent

interface ScorePolicy {
    fun supports(event: RankingEvent): Boolean
    fun calculate(event: RankingEvent): Double
}
