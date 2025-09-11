package com.loopers.support.cache

import org.springframework.stereotype.Component

@Component
class GenericCacheRepository(
    private val cacheStore: CacheStore,
) {
    fun evict(key: String) {
        cacheStore.delete(key)
    }
}
