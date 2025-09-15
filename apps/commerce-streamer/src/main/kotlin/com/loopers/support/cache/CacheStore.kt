package com.loopers.support.cache

import java.time.Duration

interface CacheStore {
    fun incrementZSetScore(key: String, value: String, score: Double)
    fun expire(key: String, ttl: Duration)
    fun delete(key: String)
}
