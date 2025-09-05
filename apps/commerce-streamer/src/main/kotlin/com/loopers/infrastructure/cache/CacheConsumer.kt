package com.loopers.infrastructure.cache

import com.loopers.domain.event.EventEnvelope
import com.loopers.domain.eventHandled.EventHandledService
import com.loopers.infrastructure.event.dto.ProductLikeChanged
import com.loopers.infrastructure.event.dto.ProductSalesChanged
import com.loopers.support.cache.GenericCacheRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class CacheConsumer(
    private val eventHandledService: EventHandledService,
    private val cacheRepository: GenericCacheRepository,
) {
    @KafkaListener(topics = ["like-events"], groupId = "cache-evict")
    fun onLikeChanged(env: EventEnvelope<ProductLikeChanged>, ack: Acknowledgment) {
        if (!eventHandledService.tryHandle(env.eventId, "cache-like")) {
            ack.acknowledge()
            return
        }

        // 캐시 키 빌더를 통한 캐시 삭제, 다만 현재는 라이크를 캐시하지 않음으로 임시 값 부여
        cacheRepository.evict("temp-like-key")
        ack.acknowledge()
    }

    @KafkaListener(topics = ["product-salse-events"], groupId = "cache-evict")
    fun onStockAdjusted(env: EventEnvelope<ProductSalesChanged>, ack: Acknowledgment) {
        if (!eventHandledService.tryHandle(env.eventId, "cache-product-salse")) {
            ack.acknowledge()
            return
        }

        // 캐시 키 빌더를 통한 캐시 삭제, 다만 현재는 상품 및 재고를 캐시하지 않음으로 임시 값 부여
        cacheRepository.evict("temp-stock-key")
        ack.acknowledge()
    }
}
