package com.loopers.application.payment

import com.loopers.application.order.OrderStateService
import com.loopers.domain.order.OrderItemService
import com.loopers.domain.order.OrderService
import com.loopers.domain.order.entity.Order
import com.loopers.domain.order.entity.OrderItem
import com.loopers.domain.payment.PaymentService
import com.loopers.domain.payment.entity.Payment
import com.loopers.domain.payment.strategy.PaymentStrategyRegistry
import com.loopers.domain.payment.strategy.PaymentStrategyResult
import com.loopers.domain.product.ProductStockService
import com.loopers.domain.product.dto.command.ProductStockCommand
import com.loopers.domain.product.dto.result.ProductStockResult
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
    private val productStockService: ProductStockService,
    private val paymentStateService: PaymentStateService,
    private val orderStateService: OrderStateService,
    private val paymentStrategyRegistry: PaymentStrategyRegistry,
) {
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun process(id: Long) {
        paymentStateService.paymentProcessing(id)

        val payment = paymentService.get(id)
        val order = orderService.get(payment.orderId)
        try {
            processPayment(payment, order)
            processSuccess(payment, order)
        } catch (e: Exception) {
            processFailure(order.id, payment.id, resolveFailureReason(e))
            throw e
        }
    }

    private fun processPayment(payment: Payment, order: Order) {
        val orderItems = orderItemService.findAll(order.id)
        val decreaseStocks = getDecreaseStocks(orderItems)
        productStockService.decreaseStocks(decreaseStocks.toCommand())

        val paymentStrategy = paymentStrategyRegistry.of(payment.paymentMethod)
        val paymentRequestResult = paymentStrategy.process(order, payment)

        when (paymentRequestResult.status) {
            PaymentStrategyResult.Status.SUCCESS -> {}
            PaymentStrategyResult.Status.FAILURE -> {
                throw CoreException(ErrorType.PAYMENT_REQUEST_FAILURE, paymentRequestResult.reason)
            }
        }
    }

    private fun processSuccess(payment: Payment, order: Order) {
        payment.success()
        order.success()
    }

    private fun processFailure(orderId: Long, paymentId: Long, reason: String) {
        paymentStateService.paymentFailure(paymentId, reason)
        orderStateService.orderFailure(orderId, reason)
    }

    private fun getDecreaseStocks(orderItems: List<OrderItem>): ProductStockResult.DecreaseStocks {
        val command = ProductStockCommand.GetDecreaseStock(
            orderItems.map {
                ProductStockCommand.GetDecreaseStock.DecreaseStock(it.productOptionId, it.quantity.value)
            },
        )
        return productStockService.getDecreaseStock(command)
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
