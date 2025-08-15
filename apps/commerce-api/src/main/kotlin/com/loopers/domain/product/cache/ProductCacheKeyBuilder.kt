package com.loopers.domain.product.cache

import com.loopers.support.cache.CacheKeyBuilder

object ProductCacheKeyBuilder : CacheKeyBuilder {
    override fun buildKey(namespace: String, version: Long?, args: Map<String, Any?>): String {
        val version = version ?: 0L
        val brandId = args["brandId"] ?: "all"
        val sort = args["sort"] ?: "LATEST"
        val page = args["page"] ?: 0
        val size = args["size"] ?: 20
        return "$namespace:version:$version:brandId:$brandId:sort:$sort:page:$page:size:$size"
    }
}
