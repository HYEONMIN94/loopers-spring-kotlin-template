package com.loopers.support.cache

interface CacheKeyBuilder {
    fun buildKey(namespace: String, version: Long?, args: Map<String, Any?>): String
}
