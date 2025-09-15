package com.loopers.support.cache.zset

interface ZSetCacheReader {
    fun rangeWithScores(key: String, start: Long, end: Long): List<Pair<String, Double>>
    fun rank(key: String, member: String): Int?
}
