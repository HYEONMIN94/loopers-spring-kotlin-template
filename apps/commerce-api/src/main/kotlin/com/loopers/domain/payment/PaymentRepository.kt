package com.loopers.domain.payment

import com.loopers.domain.payment.entity.Payment

interface PaymentRepository {
    fun find(id: Long): Payment?

    fun find(transactionKey: String): Payment?

    fun save(payment: Payment): Payment

    fun findByStatus(status: Payment.Status): List<Payment>
}
