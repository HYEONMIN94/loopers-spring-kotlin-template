package com.loopers.application.order.listener

import com.loopers.application.order.OrderStateService
import com.loopers.domain.order.event.OrderEvent
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@SpringBootTest
class OrderEventListenerTest {
    @Autowired lateinit var publisher: ApplicationEventPublisher

    @Autowired lateinit var transactionManager: PlatformTransactionManager

    @MockitoBean lateinit var orderStateService: OrderStateService

    @Test
    fun `OrderSucceededEvent는 커밋 이후 success 처리된다`() {
        val orderId = 1L

        TransactionTemplate(transactionManager).execute {
            publisher.publishEvent(OrderEvent.OrderSucceededEvent(orderId))
        }

        verify(orderStateService).orderSuccess(orderId)
    }

    @Test
    fun `OrderFailedEvent는 롤백 이후 failure 처리된다`() {
        val orderId = 1L

        runCatching {
            TransactionTemplate(transactionManager).execute {
                publisher.publishEvent(OrderEvent.OrderFailedEvent(orderId, "boom"))
                error("force rollback")
            }
        }

        verify(orderStateService).orderFailure(org.mockito.kotlin.eq(orderId), any())
    }
}
