package com.loopers.application.like.listener

import com.loopers.domain.like.LikeCountService
import com.loopers.domain.like.event.LikeEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class LikeEventListener(
    private val likeCountService: LikeCountService,
) {
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: LikeEvent.IncreaseEvent) {
        val likeCount = likeCountService.getLikeCountWithLock(event.targetId, event.type)
        likeCount.increase()
    }
}
