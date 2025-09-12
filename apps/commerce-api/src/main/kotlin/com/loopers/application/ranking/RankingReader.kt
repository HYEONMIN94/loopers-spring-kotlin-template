package com.loopers.application.ranking

import com.loopers.support.cache.zset.ZSetCacheReader
import org.springframework.stereotype.Component

@Component
class RankingReader(
    private val zSetCacheReader: ZSetCacheReader,
) {
    fun readRange(key: String, start: Int, end: Int): List<Pair<String, Double>> {
        return zSetCacheReader.rangeWithScores(key, start.toLong(), end.toLong())
    }

    fun readRank(key: String, productId: String): Int? {
        return zSetCacheReader.rank(key, productId)
    }
}
