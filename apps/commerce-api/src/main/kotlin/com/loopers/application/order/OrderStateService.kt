package com.loopers.application.order

import com.loopers.domain.order.OrderService
import com.loopers.domain.order.entity.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class OrderStateService(
    private val orderService: OrderService,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun orderSuccess(orderId: Long): Order {
        val order = orderService.get(orderId)
        order.success()
        return order
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun orderFailure(orderId: Long, reason: String): Order {
        val order = orderService.get(orderId)
        order.failure(reason)
        return order
    }
}
