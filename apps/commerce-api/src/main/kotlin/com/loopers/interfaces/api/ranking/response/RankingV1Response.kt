package com.loopers.interfaces.api.ranking.response

import com.loopers.domain.renking.result.RankingResult
import com.loopers.domain.renking.result.RankingResult.RankingDetail
import java.math.BigDecimal
import java.time.ZonedDateTime

class RankingV1Response {
    data class RankingResponse(
        val rank: Int?,
        val score: Double,
        val product: ProductResponse,
    ) {
        companion object {
            fun from(details: RankingDetail): RankingResponse {
                return RankingResponse(
                    rank = details.rank,
                    score = details.score,
                    product = ProductResponse.from(details.product),
                )
            }

            fun fromList(details: List<RankingDetail>): List<RankingResponse> {
                return details.map { from(it) }
            }
        }
    }

    data class RankingsResponse(
        val rankings: List<RankingResponse>,
    ) {
        companion object {
            fun from(details: List<RankingDetail>): RankingsResponse {
                return RankingsResponse(
                    rankings = RankingResponse.fromList(details),
                )
            }
        }
    }

    data class ProductResponse(
        val id: Long,
        val brandId: Long,
        val name: String,
        val description: String,
        val price: BigDecimal,
        val createAt: ZonedDateTime,
        val updateAt: ZonedDateTime,
    ) {
        companion object {
            fun from(product: RankingResult.ProductDetail): ProductResponse {
                return ProductResponse(
                    product.id,
                    product.brandId,
                    product.name,
                    product.description,
                    product.price,
                    product.createAt,
                    product.updateAt,
                )
            }
        }
    }
}
