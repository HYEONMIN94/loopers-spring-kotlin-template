package com.loopers.support.cache.zset

import com.loopers.config.redis.RedisConfig.Companion.REDIS_TEMPLATE_READ
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisZSetCacheReader(
    @Qualifier(REDIS_TEMPLATE_READ)
    private val redisTemplate: RedisTemplate<String, String>,
) : ZSetCacheReader {
    override fun rangeWithScores(key: String, start: Long, end: Long): List<Pair<String, Double>> {
        return redisTemplate.opsForZSet()
            .reverseRangeWithScores(key, start, end)
            ?.mapNotNull { it.value?.let { v -> v to (it.score ?: 0.0) } }
            ?: emptyList()
    }

    override fun rank(key: String, member: String): Int? {
        return redisTemplate.opsForZSet().reverseRank(key, member)?.toInt()?.plus(1)
    }
}
