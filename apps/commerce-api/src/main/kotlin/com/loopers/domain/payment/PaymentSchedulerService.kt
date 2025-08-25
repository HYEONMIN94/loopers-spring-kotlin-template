package com.loopers.domain.payment

import com.loopers.domain.order.OrderService
import com.loopers.domain.payment.entity.Payment
import com.loopers.domain.payment.type.TransactionStatus
import com.loopers.infrastructure.pg.PgClient
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class PaymentSchedulerService(
    private val paymentService: PaymentService,
    private val orderService: OrderService,
    private val pgClient: PgClient,
) {
    data class PgSchedulerResponse(
        val status: TransactionStatus?,
        val transactionKey: String?,
        val reason: String?,
    )

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun reconcileOne(paymentId: Long) {
        val payment = paymentService.get(paymentId)

        if (payment.status != Payment.Status.PROCESSING) {
            return
        }

        val order = orderService.get(payment.orderId)
        val userId = order.userId

        val snap: PgSchedulerResponse = try {
            if (payment.transactionKey != null) {
                val response = pgClient.getTransaction(userId, payment.transactionKey!!)
                val data = response.data
                PgSchedulerResponse(
                    status = data?.status,
                    transactionKey = data?.transactionKey ?: payment.transactionKey,
                    reason = data?.reason,
                )
            } else {
                val response = pgClient.getOrder(userId, "LOOPERS-${order.id}")
                val transactions = response.data?.transactions?.firstOrNull()
                PgSchedulerResponse(
                    status = transactions?.status,
                    transactionKey = transactions?.transactionKey,
                    reason = transactions?.reason,
                )
            }
        } catch (e: Exception) {
            return
        }

        if (payment.transactionKey == null && !snap.transactionKey.isNullOrBlank()) {
            payment.updateTransactionKey(snap.transactionKey)
        }

        when (snap.status) {
            TransactionStatus.SUCCESS -> {
                payment.success()
                order.success()
            }
            TransactionStatus.FAILED -> {
                val reason = snap.reason
                payment.failure(reason)
                order.failure(reason)
            }
            TransactionStatus.PENDING, null -> {
                // TODO: 그럼에도 불구하고 펜딩상태면... PG사쪽에 문의가 필요하지 않을까 싶습니다
            }
        }
    }
}
