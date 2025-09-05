package com.loopers.domain.productMetrics

import com.loopers.domain.productMetrics.entity.ProductMetrics
import java.time.LocalDate

interface ProductMetricsRepository {
    fun findByMetricsDateAndProductId(metricsDate: LocalDate, productId: Long): ProductMetrics?

    fun upsertAdd(metricsDate: LocalDate, productId: Long, likes: Long, sales: Long, views: Long): Int
}
