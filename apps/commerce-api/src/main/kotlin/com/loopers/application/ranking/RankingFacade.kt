package com.loopers.application.ranking

import com.loopers.domain.product.ProductService
import com.loopers.domain.renking.result.RankingResult.RankingDetail
import org.springframework.stereotype.Component

@Component
class RankingFacade(
    private val rankingReader: RankingReader,
    private val productService: ProductService,
) {
    fun getRankings(date: String, size: Int, page: Int): List<RankingDetail> {
        val key = "ranking:all:$date"
        val start = (page - 1) * size
        val end = start + size - 1

        return rankingReader.readRange(key, start, end)
            .mapIndexed { index, (productId, score) ->
                val product = productService.get(productId.toLong())
                RankingDetail.from(
                    rank = rankingReader.readRank(key, productId),
                    score = score,
                    product = product,
                )
            }
    }
}
