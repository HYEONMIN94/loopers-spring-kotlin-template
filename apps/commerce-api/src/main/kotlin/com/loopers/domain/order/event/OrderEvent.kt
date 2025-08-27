package com.loopers.domain.order.event

class OrderEvent {
    data class OrderSucceededEvent(
        val orderId: Long,
    )

    data class OrderFailedEvent(
        val orderId: Long,
        val reason: String,
    )
}
