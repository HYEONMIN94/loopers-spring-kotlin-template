package com.loopers.support.cache

import java.time.Duration

interface CachePolicy {
    val namespace: String
    fun ttlFor(kind: String): Duration?
    fun versionKey(args: Map<String, Any?> = emptyMap()): String
}
