package com.loopers.application.like.listener

import com.loopers.domain.like.LikeCountService
import com.loopers.domain.like.entity.LikeCount
import com.loopers.domain.like.event.LikeEvent
import com.loopers.domain.like.vo.LikeTarget.Type
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.Duration

@SpringBootTest
class LikeEventListenerTest {
    @Autowired lateinit var publisher: ApplicationEventPublisher

    @Autowired lateinit var transactionManager: PlatformTransactionManager

    @MockkBean lateinit var likeCountService: LikeCountService

    @Test
    fun `좋아요 증가 이벤트는 리스너에서 카운트를 증가시킨다`() {
        // given
        val targetId = 100L
        val type = Type.PRODUCT

        val likeCount = mockk<LikeCount>(relaxed = true)
        every { likeCountService.getLikeCountWithLock(targetId, type) } returns likeCount

        // when
        TransactionTemplate(transactionManager).execute {
            publisher.publishEvent(LikeEvent.IncreaseEvent(targetId, type))
        }

        // then
        await().atMost(Duration.ofSeconds(10)).untilAsserted {
            verify(exactly = 1) { likeCountService.getLikeCountWithLock(targetId, type) }
            verify(exactly = 1) { likeCount.increase() }
        }
    }
}
