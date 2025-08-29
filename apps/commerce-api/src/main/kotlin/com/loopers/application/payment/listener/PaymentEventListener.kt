package com.loopers.application.payment.listener

import com.loopers.application.payment.PaymentStateService
import com.loopers.domain.payment.PaymentService
import com.loopers.domain.payment.event.PaymentEvent
import com.loopers.domain.payment.strategy.PaymentStrategyRegistry
import com.loopers.domain.payment.strategy.PaymentStrategyResult
import com.loopers.infrastructure.dataFlaform.DataFlatformPublisher
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PaymentEventListener(
    private val paymentService: PaymentService,
    private val paymentStateService: PaymentStateService,
    private val paymentStrategyRegistry: PaymentStrategyRegistry,
    private val dataFlatformPublisher: DataFlatformPublisher,
) {
    @EventListener
    fun handle(event: PaymentEvent.PaymentRequestEvent) {
        val payment = paymentService.get(event.paymentId)
        val paymentStrategy = paymentStrategyRegistry.of(payment.paymentMethod)
        val paymentRequestResult = paymentStrategy.process(event.userId, payment)
        when (paymentRequestResult.status) {
            PaymentStrategyResult.Status.SUCCESS -> {}
            PaymentStrategyResult.Status.FAILURE -> {
                throw CoreException(ErrorType.PAYMENT_REQUEST_FAILURE, paymentRequestResult.reason)
            }
        }
    }

    @EventListener
    fun handle(event: PaymentEvent.PaymentProcessedEvent) {
        paymentStateService.paymentProcessing(event.paymentId)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: PaymentEvent.PaymentSucceededEvent) {
        val payment = paymentStateService.paymentSuccess(event.paymentId)
        dataFlatformPublisher.publish(payment)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    fun handle(event: PaymentEvent.PaymentFailedEvent) {
        val payment = paymentStateService.paymentFailure(event.paymentId, event.reason)
        dataFlatformPublisher.publish(payment)
    }
}
