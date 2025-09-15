package com.loopers.domain.renking.event

import com.loopers.domain.renking.type.EventType

data class RankingEvent(
    val productId: Long,
    val eventType: EventType,
    val price: Long? = null,
    val amount: Int? = null,
)
