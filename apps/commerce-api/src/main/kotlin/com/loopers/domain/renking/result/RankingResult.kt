package com.loopers.domain.renking.result

import com.loopers.domain.product.entity.Product
import java.math.BigDecimal
import java.time.ZonedDateTime

class RankingResult {
    data class RankingDetail(
        val rank: Int?,
        val score: Double,
        val product: ProductDetail,
    ) {
        companion object {
            fun from(rank: Int?, score: Double, product: Product): RankingDetail {
                return RankingDetail(rank, score, ProductDetail.from(product))
            }
        }
    }

    data class ProductDetail(
        val id: Long,
        val brandId: Long,
        val name: String,
        val description: String,
        val price: BigDecimal,
        val createAt: ZonedDateTime,
        val updateAt: ZonedDateTime,
    ) {
        companion object {
            fun from(product: Product): ProductDetail {
                return ProductDetail(
                    product.id,
                    product.brandId,
                    product.name.value,
                    product.description.value,
                    product.price.value,
                    product.createdAt,
                    product.updatedAt,
                )
            }
        }
    }
}
