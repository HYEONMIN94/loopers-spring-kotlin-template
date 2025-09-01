package com.loopers.domain.payment.event

class PaymentEvent {
    data class PaymentRequestEvent(
        val userId: Long,
        val paymentId: Long,
    )

    data class PaymentProcessedEvent(
        val paymentId: Long,
    )

    data class PaymentSucceededEvent(
        val paymentId: Long,
    )

    data class PaymentFailedEvent(
        val paymentId: Long,
        val reason: String,
    )
}
