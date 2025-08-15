package com.loopers.domain.product

import com.fasterxml.jackson.core.type.TypeReference
import com.loopers.domain.product.dto.command.ProductCommand
import com.loopers.domain.product.dto.criteria.ProductCriteria
import com.loopers.domain.product.entity.Product
import com.loopers.domain.product.policy.ProductCachePolicy
import com.loopers.support.cache.CacheKeyDsl
import com.loopers.support.cache.GenericCacheRepository
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class ProductService(
    private val productRepository: ProductRepository,
    private val cacheRepository: GenericCacheRepository,
) {
    fun get(id: Long): Product {
        return productRepository.find(id)
            ?: throw CoreException(errorType = ErrorType.NOT_FOUND, customMessage = "[id = $id] 예시를 찾을 수 없습니다.")
    }

    fun findAll(ids: List<Long>): List<Product> {
        return productRepository.findAll(ids)
    }

    fun register(command: ProductCommand.RegisterProduct): Product {
        return productRepository.save(command.toEntity())
    }

    fun findAllCached(criteria: ProductCriteria.FindAll): Page<Product> {
        val args = mutableMapOf<String, Any?>(
            "sort" to criteria.sort.name,
            "page" to criteria.page,
            "size" to criteria.size,
        ).apply {
            when {
                criteria.brandIds.isEmpty() -> put("brandId", null)
                criteria.brandIds.size == 1 -> put("brandId", criteria.brandIds.first())
                else -> put("brandId", null) // 다수 브랜드는 버전키 all로 단순화
            }
        }

        val listKey = CacheKeyDsl("product:list")
            .part("brandId", args["brandId"])
            .part("sort", args["sort"])
            .part("page", args["page"])
            .part("size", args["size"])

        val list: List<Product> = cacheRepository.cacheAside(
            kind = "list",
            policy = ProductCachePolicy,
            cacheKeyDsl = listKey,
            args = args,
            typeRef = object : TypeReference<List<Product>>() {},
        ) {
            productRepository.findAll(criteria)
        }

        val countKey = CacheKeyDsl("product:list")
            .part("brandId", args["brandId"])
            .part("kind", "count")

        val total: Long = cacheRepository.cacheAside(
            kind = "count",
            policy = ProductCachePolicy,
            cacheKeyDsl = countKey,
            args = args,
            typeRef = object : TypeReference<Long>() {},
        ) {
            productRepository.count(criteria)
        }

        val pageable = PageRequest.of(criteria.page, criteria.size)
        return PageImpl(list, pageable, total)
    }
}
