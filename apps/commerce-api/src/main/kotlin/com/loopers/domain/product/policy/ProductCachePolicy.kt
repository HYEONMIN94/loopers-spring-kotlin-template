package com.loopers.domain.product.policy

import com.loopers.support.cache.CachePolicy
import java.time.Duration

object ProductCachePolicy : CachePolicy {
    override val namespace: String = "product:list"

    override fun ttlFor(kind: String): Duration = when (kind) {
        "list" -> Duration.ofMinutes(3)
        "count" -> Duration.ofMinutes(5)
        else -> Duration.ofMinutes(3)
    }

    override fun versionKey(args: Map<String, Any?>): String {
        val brandId = args["brandId"] as? Long
        return if (brandId != null) {
            "v:$namespace:brand:$brandId"
        } else {
            "v:$namespace:all"
        }
    }
}
