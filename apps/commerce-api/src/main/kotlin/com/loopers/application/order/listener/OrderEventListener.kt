package com.loopers.application.order.listener

import com.loopers.application.order.OrderStateService
import com.loopers.domain.order.event.OrderEvent
import com.loopers.infrastructure.dataFlaform.DataFlatformPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OrderEventListener(
    private val orderStateService: OrderStateService,
    private val dataFlatformPublisher: DataFlatformPublisher,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: OrderEvent.OrderSucceededEvent) {
        val order = orderStateService.orderSuccess(event.orderId)
        dataFlatformPublisher.publish(order)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    fun handle(event: OrderEvent.OrderFailedEvent) {
        val order = orderStateService.orderFailure(event.orderId, event.reason)
        dataFlatformPublisher.publish(order)
    }
}
