package com.loopers.domain.like.event

import com.loopers.domain.like.vo.LikeTarget.Type

class LikeEvent {
    data class IncreaseEvent(
        val targetId: Long,
        val type: Type,
    )

    data class DecreaseEvent(
        val targetId: Long,
        val type: Type,
    )
}
