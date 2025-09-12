package com.loopers.application.product

import com.loopers.application.product.publisher.ProductEventPublisher
import com.loopers.application.ranking.RankingReader
import com.loopers.application.ranking.publisher.RankingEventPublisher
import com.loopers.domain.brand.BrandService
import com.loopers.domain.brand.dto.result.BrandResult
import com.loopers.domain.like.LikeCountService
import com.loopers.domain.like.dto.command.LikeCountCommand
import com.loopers.domain.like.dto.result.LikeCountResult
import com.loopers.domain.like.vo.LikeTarget.Type.PRODUCT
import com.loopers.domain.product.ProductService
import com.loopers.domain.product.dto.command.ProductCommand
import com.loopers.domain.product.dto.criteria.ProductCriteria
import com.loopers.domain.product.dto.result.ProductResult
import com.loopers.domain.product.dto.result.ProductResult.ProductDetail
import com.loopers.domain.product.dto.result.ProductWithResult.PageWithBrandDetails
import com.loopers.domain.product.dto.result.ProductWithResult.WithBrandDetail
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Transactional(readOnly = true)
@Component
class ProductFacade(
    private val productService: ProductService,
    private val brandService: BrandService,
    private val likeCountService: LikeCountService,
    private val productEventPublisher: ProductEventPublisher,
    private val rankingEventPublisher: RankingEventPublisher,
    private val rankingReader: RankingReader,
) {
    fun getProduct(productId: Long): WithBrandDetail {
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val rankKey = "ranking:all:$date"

        val productDetail = ProductDetail.from(
            productService.get(productId),
            rankingReader.readRank(rankKey, productId.toString()),
        )
        val brandDetail = BrandResult.BrandDetail.from(brandService.get(productDetail.brandId))
        val likeCountDetail = likeCountService.getLikeCount(productId, PRODUCT)
            .let { LikeCountResult.LikeCountDetail.from(it) }

        productEventPublisher.publishProductViewed(productDetail.id, 1)
        rankingEventPublisher.publishViewEvent(productId)
        return WithBrandDetail.from(productDetail, brandDetail, likeCountDetail)
    }

    fun getProducts(criteria: ProductCriteria.FindAll): PageWithBrandDetails {
        val productPage = productService.findAllCached(criteria)
        val productDetails = ProductResult.ProductPageDetails.from(productPage)

        val brandIds = criteria.brandIds.takeIf { it.isNotEmpty() }
            ?: productDetails.products.data.map { it.brandId }.distinct()
        val brands = brandService.findAll(brandIds)
        val brandDetails = BrandResult.BrandDetails.from(brands)

        val productIds = productDetails.products.data.map { it.id }
        val likes = likeCountService.getLikeCounts(productIds, PRODUCT)
        val likeCountDetails = LikeCountResult.LikeCountDetails.from(likes)

        return PageWithBrandDetails.from(productDetails, brandDetails, likeCountDetails)
    }

    @Transactional
    fun registerProduct(command: ProductCommand.RegisterProduct): ProductDetail {
        val product = productService.register(command)
        likeCountService.register(LikeCountCommand.Register(product.id, PRODUCT, 0))
        return ProductDetail.from(product)
    }
}
