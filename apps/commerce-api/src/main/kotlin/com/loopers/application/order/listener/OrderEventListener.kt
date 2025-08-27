package com.loopers.application.order.listener

import com.loopers.application.order.OrderStateService
import com.loopers.domain.order.event.OrderEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OrderEventListener(
    private val orderStateService: OrderStateService,
) {
    @EventListener
    fun handle(event: OrderEvent.OrderSucceededEvent) {
        orderStateService.orderSuccess(event.orderId)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    fun handle(event: OrderEvent.OrderFailedEvent) {
        orderStateService.orderFailure(event.orderId, event.reason)
    }
}
