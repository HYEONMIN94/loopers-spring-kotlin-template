package com.loopers.domain.productMetrics

import jakarta.transaction.Transactional
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ProductMetricsService(
    private val productMetricsRepository: ProductMetricsRepository,
) {
    @Transactional
    fun addMetrics(
        date: LocalDate,
        productId: Long,
        likesDelta: Long,
        salesDelta: Long,
        viewsDelta: Long,
    ) {
        productMetricsRepository.upsertAdd(date, productId, likesDelta, salesDelta, viewsDelta)
    }
}
