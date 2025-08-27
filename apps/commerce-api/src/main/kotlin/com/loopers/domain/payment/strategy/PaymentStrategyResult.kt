package com.loopers.domain.payment.strategy

class PaymentStrategyResult(
    val status: Status,
    val reason: String?,
) {
    companion object {
        fun success() = PaymentStrategyResult(
            status = Status.SUCCESS,
            reason = null,
        )

        fun failure(reason: String?) = PaymentStrategyResult(
            status = Status.FAILURE,
            reason = reason,
        )
    }

    enum class Status {
        SUCCESS,
        FAILURE,
    }
}
