package com.loopers.application.payment

import com.loopers.application.order.publisher.OrderEventPublisher
import com.loopers.domain.order.OrderItemService
import com.loopers.domain.order.OrderService
import com.loopers.domain.order.entity.OrderItem
import com.loopers.domain.order.event.OrderEvent
import com.loopers.domain.payment.PaymentService
import com.loopers.domain.payment.entity.Payment
import com.loopers.domain.payment.event.PaymentEvent
import com.loopers.domain.product.ProductOptionService
import com.loopers.domain.product.event.ProductStockEvent
import com.loopers.infrastructure.event.DomainEventPublisher
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class PaymentProcessor(
    private val paymentService: PaymentService,
    private val orderService: OrderService,
    private val orderItemService: OrderItemService,
    private val productOptionService: ProductOptionService,
    private val eventPublisher: DomainEventPublisher,
    private val orderEventPublisher: OrderEventPublisher,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun process(id: Long) {
        eventPublisher.publish(PaymentEvent.PaymentProcessedEvent(id))

        val payment = paymentService.get(id)
        val order = orderService.get(payment.orderId)
        try {
            val orderItems = orderItemService.findAll(order.id)

            // 재고 차감
            eventPublisher.publish(decreaseStocksEvents(orderItems))

            // 실 결제 진행
            eventPublisher.publish(PaymentEvent.PaymentRequestEvent(order.userId, payment.id))

            if (payment.paymentMethod == Payment.Method.POINT) {
                eventPublisher.publish(PaymentEvent.PaymentSucceededEvent(payment.id))
                eventPublisher.publish(OrderEvent.OrderSucceededEvent(order.id))
            }

            publishProductSalse(order.id)
        } catch (e: Exception) {
            val reason = resolveFailureReason(e)
            eventPublisher.publish(PaymentEvent.PaymentFailedEvent(payment.id, reason))
            eventPublisher.publish(OrderEvent.OrderFailedEvent(order.id, reason))
            throw e
        }
    }

    private fun publishProductSalse(orderId: Long) {
        val items = orderItemService.findAll(orderId)

        val optionIds = items.map { it.productOptionId }.distinct()
        val options = productOptionService.findAll(optionIds)
        val optionToProductId = options.associate { it.id to it.productId }

        val perProductQty: Map<Long, Long> = items
            .groupBy { optionToProductId[it.productOptionId] ?: error("상품 옵션이 매칭되지 않습니다 =${it.productOptionId}") }
            .mapValues { (_, lines) -> lines.sumOf { it.quantity.value.toLong() } }

        perProductQty.forEach { (productId, qty) ->
            orderEventPublisher.publishProductSalse(productId, qty)
        }
    }

    private fun decreaseStocksEvents(orderItems: List<OrderItem>): ProductStockEvent.DecreaseStocksEvent {
        return ProductStockEvent.DecreaseStocksEvent(
            orderItems.map {
                ProductStockEvent.DecreaseStockEvent(it.productOptionId, it.quantity.value)
            },
        )
    }

    private fun resolveFailureReason(e: Throwable): String {
        // TODO: 개선 필요합니다... 과제에 집중하기 위해서 일단 이렇게 처리합니다ㅜㅜ

        return when (e) {
            is CoreException -> when (e.errorType) {
                ErrorType.POINT_NOT_ENOUGH -> "포인트가 부족합니다."
                ErrorType.PRODUCT_STOCK_NOT_ENOUGH -> "재고가 부족합니다."
                else -> e.message.toString()
            }
            is ObjectOptimisticLockingFailureException -> "동시 요청으로 인한 에러가 발생했습니다."
            else -> "알 수 없는 예외가 발생했습니다."
        }
    }
}
