package com.loopers.application.payment.listener

import com.loopers.application.payment.PaymentStateService
import com.loopers.domain.payment.event.PaymentEvent
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

@SpringBootTest
class PaymentEventListenerTest {
    @Autowired lateinit var publisher: ApplicationEventPublisher

    @Autowired lateinit var transactionManager: PlatformTransactionManager

    @MockitoBean lateinit var paymentStateService: PaymentStateService

    @Test
    fun `PaymentProcessedEvent는 동기적으로 processing 처리된다`() {
        val paymentId = 1L

        publisher.publishEvent(PaymentEvent.PaymentProcessedEvent(paymentId))

        verify(paymentStateService).paymentProcessing(paymentId)
    }

    @Test
    fun `PaymentSucceededEvent는 커밋 이후 success 처리된다`() {
        val paymentId = 1L

        TransactionTemplate(transactionManager).execute {
            publisher.publishEvent(PaymentEvent.PaymentSucceededEvent(paymentId))
        }

        verify(paymentStateService).paymentSuccess(paymentId)
    }

    @Test
    fun `PaymentFailedEvent는 롤백 이후 failure 처리된다`() {
        val paymentId = 1L

        runCatching {
            TransactionTemplate(transactionManager).execute {
                publisher.publishEvent(PaymentEvent.PaymentFailedEvent(paymentId, "rollback"))
                error("rollback")
            }
        }

        verify(paymentStateService).paymentFailure(eq(paymentId), any())
    }
}
