package com.loopers.domain.productMetrics.entity

import com.loopers.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDate

@Entity
@Table(
    name = "product_metrics",
    uniqueConstraints = [UniqueConstraint(columnNames = ["metrics_date", "product_id"])],
)
class ProductMetrics protected constructor(
    metricsDate: LocalDate,
    productId: Long,
) : BaseEntity() {
    @Column(name = "metrics_date", nullable = false)
    var metricsDate: LocalDate = metricsDate
        protected set

    @Column(name = "product_id", nullable = false)
    var productId: Long = productId
        protected set

    @Column(name = "likes", nullable = false)
    var likes: Long = 0

    @Column(name = "sales", nullable = false)
    var sales: Long = 0

    @Column(name = "views", nullable = false)
    var views: Long = 0

    fun incLikes(delta: Long) {
        likes += delta
    }
    fun incSales(delta: Long) {
        sales += delta
    }
    fun incViews(delta: Long) {
        views += delta
    }
}
