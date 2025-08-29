package com.loopers.application.payment

import com.loopers.domain.order.OrderItemService
import com.loopers.domain.order.OrderService
import com.loopers.domain.order.entity.OrderItem
import com.loopers.domain.order.event.OrderEvent
import com.loopers.domain.payment.PaymentService
import com.loopers.domain.payment.dto.command.PaymentCommand
import com.loopers.domain.payment.dto.result.PaymentResult
import com.loopers.domain.payment.event.PaymentEvent
import com.loopers.domain.payment.type.TransactionStatus
import com.loopers.domain.product.ProductOptionService
import com.loopers.domain.product.ProductService
import com.loopers.domain.product.entity.ProductOption
import com.loopers.infrastructure.event.DomainEventPublisher
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Transactional(readOnly = true)
@Component
class PaymentFacade(
    private val paymentProcessor: PaymentProcessor,
    private val paymentService: PaymentService,
    private val orderService: OrderService,
    private val orderItemService: OrderItemService,
    private val productOptionService: ProductOptionService,
    private val productService: ProductService,
    private val eventPublisher: DomainEventPublisher,
) {
    @Transactional
    fun requestPayment(command: PaymentCommand.Request): PaymentResult.PaymentDetail {
        val orderItems = orderItemService.findAll(command.orderId)
        val productOptions = loadProductOptions(orderItems)
        val paymentPrice = calculateTotalPrice(orderItems, productOptions)
        val payment = paymentService.request(command.toEntity(paymentPrice))
        val order = orderService.get(payment.id)
        order.paymentRequest()
        return PaymentResult.PaymentDetail.from(payment)
    }

    @Transactional
    fun processPayment(id: Long) {
        // TODO: 해당 유저의 결제 요청이 맞는지 체크
        paymentProcessor.process(id)
    }

    @Transactional
    fun processPaymentWebhook(command: PaymentCommand.PaymentWebhook) {
        val payment = paymentService.get(command.transactionKey)
        val order = orderService.get(payment.orderId)
        if (command.status == TransactionStatus.SUCCESS) {
            eventPublisher.publish(PaymentEvent.PaymentSucceededEvent(payment.id))
            eventPublisher.publish(OrderEvent.OrderSucceededEvent(order.id))
        } else if (command.status == TransactionStatus.FAILED) {
            eventPublisher.publish(PaymentEvent.PaymentFailedEvent(payment.id, command.reason!!))
            eventPublisher.publish(OrderEvent.OrderFailedEvent(order.id, command.reason))
            throw CoreException(ErrorType.PAYMENT_FAILURE, "결제에 실패하였습니다. 사유: ${command.reason}")
        }
    }

    private fun loadProductOptions(orderItems: List<OrderItem>): List<ProductOption> {
        val productOptionIds = orderItems.map { it.productOptionId }
        return productOptionService.findAll(productOptionIds)
    }

    private fun calculateTotalPrice(
        orderItems: List<OrderItem>,
        productOptions: List<ProductOption>,
    ): BigDecimal {
        val optionMap = productOptions.associateBy { it.id }
        val productIds = productOptions.map { it.productId }.distinct()
        val productMap = productService.findAll(productIds).associateBy { it.id }

        return orderItems.sumOf { item ->
            val option = optionMap[item.productOptionId]
                ?: throw CoreException(ErrorType.NOT_FOUND, "상품 옵션을 찾을 수 없습니다.")
            val product = productMap[option.productId]
                ?: throw CoreException(ErrorType.NOT_FOUND, "상품 정보를 찾을 수 없습니다.")

            item.calculatePrice(product.price.value, option.additionalPrice.value)
        }
    }
}
