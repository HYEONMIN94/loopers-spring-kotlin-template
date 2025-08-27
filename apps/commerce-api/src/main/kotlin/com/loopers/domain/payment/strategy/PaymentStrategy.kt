package com.loopers.domain.payment.strategy

import com.loopers.domain.payment.entity.Payment

interface PaymentStrategy {
    fun supports(): Payment.Method
    fun process(userId: Long, payment: Payment): PaymentStrategyResult
}
