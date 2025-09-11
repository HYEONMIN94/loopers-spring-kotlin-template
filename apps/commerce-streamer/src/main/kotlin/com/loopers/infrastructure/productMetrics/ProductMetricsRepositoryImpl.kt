package com.loopers.infrastructure.productMetrics

import com.loopers.domain.productMetrics.ProductMetricsRepository
import com.loopers.domain.productMetrics.entity.ProductMetrics
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ProductMetricsRepositoryImpl(
    private val productMetricsRepository: ProductMetricsJpaRepository,
) : ProductMetricsRepository {
    override fun findByMetricsDateAndProductId(
        metricsDate: LocalDate,
        productId: Long,
    ): ProductMetrics? {
        return productMetricsRepository.findByMetricsDateAndProductId(metricsDate, productId)
    }

    override fun upsertAdd(
        metricsDate: LocalDate,
        productId: Long,
        likes: Long,
        sales: Long,
        views: Long,
    ): Int {
        return productMetricsRepository.upsertAdd(metricsDate, productId, likes, sales, views)
    }
}
