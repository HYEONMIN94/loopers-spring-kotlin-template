package com.loopers.domain.ranking.event

import com.loopers.domain.ranking.type.EventType

data class RankingEvent(
    val productId: Long,
    val eventType: EventType,
    val price: Long? = null,
    val amount: Int? = null,
)
