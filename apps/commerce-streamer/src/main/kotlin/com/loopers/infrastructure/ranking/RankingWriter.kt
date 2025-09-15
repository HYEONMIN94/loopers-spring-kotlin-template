package com.loopers.infrastructure.ranking

import com.loopers.support.cache.CacheStore
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RankingWriter(
    private val cacheStore: CacheStore,
) {
    fun write(key: String, productId: String, score: Double) {
        cacheStore.incrementZSetScore(key, productId, score)
        cacheStore.expire(key, Duration.ofDays(2))
    }

    fun delete(key: String) {
        cacheStore.delete(key)
    }
}
