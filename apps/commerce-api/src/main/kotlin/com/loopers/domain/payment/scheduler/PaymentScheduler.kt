package com.loopers.domain.payment.scheduler

import com.loopers.domain.payment.PaymentRepository
import com.loopers.domain.payment.PaymentSchedulerService
import com.loopers.domain.payment.entity.Payment
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PaymentScheduler(
    private val paymentRepository: PaymentRepository,
    private val paymentSchedulerService: PaymentSchedulerService,
) {
    @Scheduled(fixedDelayString = "60000")
    fun reconcile() {
        // TODO: 시간 및 처리 데이터 테이블들 별도로 만들어 체크해야함

        val processingPayment: List<Payment> = paymentRepository.findByStatus(Payment.Status.PROCESSING)

        if (processingPayment.isEmpty()) {
            return
        }

        processingPayment.forEach {
            paymentSchedulerService.reconcileOne(it.id)
        }
    }
}
