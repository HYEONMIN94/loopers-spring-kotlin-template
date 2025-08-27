package com.loopers.domain.payment.strategy

import com.loopers.domain.payment.entity.Payment
import com.loopers.domain.point.PointService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class PointPaymentStrategy(
    private val pointService: PointService,
) : PaymentStrategy {

    override fun supports() = Payment.Method.POINT

    @Transactional
    override fun process(userId: Long, payment: Payment): PaymentStrategyResult {
        val point = pointService.get(userId)
        point.use(payment.paymentPrice.value)

        return PaymentStrategyResult.success()
    }
}
