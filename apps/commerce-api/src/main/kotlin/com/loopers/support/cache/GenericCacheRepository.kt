package com.loopers.support.cache

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class GenericCacheRepository(
    private val cacheStore: CacheStore,
    private val objectMapper: ObjectMapper,
) {
    fun <T> cacheAside(
        kind: String,
        policy: CachePolicy,
        cacheKeyDsl: CacheKeyDsl,
        args: Map<String, Any?>,
        typeRef: TypeReference<T>,
        loader: () -> T,
    ): T {
        val versionKey = policy.versionKey(args)
        val version: Long? = cacheStore.get(versionKey)?.toLongOrNull()

        val key = cacheKeyDsl.build(version)

        cacheStore.get(key)
            ?.let {
                return objectMapper.readValue(it, typeRef)
            }

        val loaded = loader()

        val ttl = policy.ttlFor(kind)
        val json = objectMapper.writeValueAsString(loaded)
        if (ttl != null) cacheStore.set(key, json, ttl) else cacheStore.set(key, json)

        return loaded
    }
}
