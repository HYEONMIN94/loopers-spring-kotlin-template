package com.loopers.support.cache

class CacheKeyDsl(private val namespace: String) {
    private val parts = mutableListOf<String>()
    fun part(k: String, v: Any?) = apply { parts += "$k:${v ?: "nil"}" }
    fun build(version: Long?) = "$namespace:v:${version ?: 0}:${parts.joinToString(":")}"
}
