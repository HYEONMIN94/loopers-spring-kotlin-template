package com.loopers.infrastructure.productMetrics

import com.loopers.domain.productMetrics.entity.ProductMetrics
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface ProductMetricsJpaRepository : JpaRepository<ProductMetrics, Long> {
    fun findByMetricsDateAndProductId(metricsDate: LocalDate, productId: Long): ProductMetrics?

    @Modifying
    @Query(
        """
        INSERT INTO product_metrics(metrics_date, product_id, likes, sales, views, created_at, updated_at)
        VALUES (:metricsDate, :productId, :likes, :sales, :views, NOW(6), NOW(6))
        ON DUPLICATE KEY UPDATE 
            likes = likes + :likes, 
            sales = sales + :sales, 
            views = views + :views,
            updated_at = NOW(6)
        """,
        nativeQuery = true,
    )
    fun upsertAdd(metricsDate: LocalDate, productId: Long, likes: Long, sales: Long, views: Long): Int
}
