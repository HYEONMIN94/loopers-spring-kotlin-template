package com.loopers.infrastructure.productMetrics

import com.loopers.domain.event.EventEnvelope
import com.loopers.domain.eventHandled.EventHandledService
import com.loopers.domain.productMetrics.ProductMetricsService
import com.loopers.infrastructure.event.dto.ProductLikeChanged
import com.loopers.infrastructure.event.dto.ProductSalesChanged
import com.loopers.infrastructure.event.dto.ProductViewed
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ProductMetricsConsumer(
    private val eventHandledService: EventHandledService,
    private val productMetricsService: ProductMetricsService,
) {
    @KafkaListener(topics = ["like-events"], groupId = "product-metrics")
    fun onLikeChanged(env: EventEnvelope<ProductLikeChanged>) {
        if (!eventHandledService.tryHandle(env.eventId, "metrics-like")) return

        val payload = env.payload
        productMetricsService.addMetrics(
            LocalDate.now(),
            payload.productId,
            likesDelta = payload.delta,
            salesDelta = 0,
            viewsDelta = 0,
        )
    }

    @KafkaListener(topics = ["product-salse-events"], groupId = "product-metrics")
    fun onSalesChanged(env: EventEnvelope<ProductSalesChanged>) {
        if (!eventHandledService.tryHandle(env.eventId, "metrics-product-salse")) return

        val payload = env.payload
        productMetricsService.addMetrics(
            LocalDate.now(),
            payload.productId,
            likesDelta = 0,
            salesDelta = payload.quantity,
            viewsDelta = 0,
        )
    }

    @KafkaListener(topics = ["product-view-events"], groupId = "product-metrics")
    fun onViewed(env: EventEnvelope<ProductViewed>) {
        if (!eventHandledService.tryHandle(env.eventId, "metrics-product-view")) return

        val payload = env.payload
        productMetricsService.addMetrics(
            LocalDate.now(),
            payload.productId,
            likesDelta = 0,
            salesDelta = 0,
            viewsDelta = payload.delta,
        )
    }
}
