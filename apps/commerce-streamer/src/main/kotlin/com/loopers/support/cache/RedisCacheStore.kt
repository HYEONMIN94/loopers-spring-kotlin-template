package com.loopers.support.cache

import com.loopers.config.redis.RedisConfig.Companion.REDIS_TEMPLATE_MASTER
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisCacheStore(
    private val redisTemplate: RedisTemplate<String, String>,
    @Qualifier(REDIS_TEMPLATE_MASTER)
    private val masterRedisTemplate: RedisTemplate<String, String>,
) : CacheStore {
    override fun delete(key: String) {
        runCatching { masterRedisTemplate.delete(key) }
    }
}
