package com.loopers.domain.payment

import com.loopers.domain.order.OrderService
import com.loopers.domain.order.entity.Order
import com.loopers.domain.payment.entity.Payment
import com.loopers.domain.payment.type.CardType
import com.loopers.domain.payment.type.TransactionStatus
import com.loopers.infrastructure.pg.PgClient
import com.loopers.infrastructure.pg.PgDto
import com.loopers.interfaces.api.ApiResponse
import com.loopers.utils.DatabaseCleanUp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.math.BigDecimal
import kotlin.String

@SpringBootTest
class PaymentSchedulerServiceIntegrationTest @Autowired constructor(
    private val scheduler: PaymentSchedulerService,
    private val paymentService: PaymentService,
    private val paymentRepository: PaymentRepository,
    private val databaseCleanUp: DatabaseCleanUp,
) {

    @MockitoBean
    private lateinit var pgClient: PgClient

    @MockitoBean
    private lateinit var orderService: OrderService

    @AfterEach
    fun tearDown() {
        databaseCleanUp.truncateAllTables()
    }

    private fun saveProcessingPayment(
        orderId: Long = 100L,
        transactionKey: String? = null,
    ): Payment {
        val p = Payment.create(
            orderId = orderId,
            paymentMethod = Payment.Method.CREDIT_CARD,
            paymentPrice = BigDecimal("1000"),
            status = Payment.Status.PROCESSING,
            cardType = "KB",
            cardNumber = "1111-2222-3333-4444",
        )
        if (transactionKey != null) p.updateTransactionKey(transactionKey)
        return paymentRepository.save(p)
    }

    private fun saveNonProcessingPayment(
        orderId: Long = 200L,
    ): Payment {
        return paymentRepository.save(
            Payment.create(
                orderId = orderId,
                paymentMethod = Payment.Method.CREDIT_CARD,
                paymentPrice = BigDecimal("500"),
                status = Payment.Status.REQUESTED,
                cardType = "KB",
                cardNumber = "1111-2222-3333-4444",
            ),
        )
    }

    private fun mockOrder(orderId: Long, userId: Long = 1L): Order {
        val order = mock<Order> {
            on { this.id }.thenReturn(orderId)
            on { this.userId }.thenReturn(userId)
        }
        given(orderService.get(orderId)).willReturn(order)
        return order
    }

    @DisplayName("PG 결과 동기화")
    @Nested
    inner class ReconcileOne {

        @Test
        fun `PG가 성공하면 결제 주문을 성공처리 한다`() {
            // given
            val payment = saveProcessingPayment(orderId = 1L, transactionKey = "TRANSACTION_KEY")
            val order = mockOrder(payment.orderId, userId = 1L)

            given(pgClient.getTransaction(order.userId, "TRANSACTION_KEY"))
                .willReturn(
                    ApiResponse.success(
                        PgDto.TransactionDetailResponse(
                            transactionKey = "TRANSACTION_KEY",
                            orderId = "LOPPERS-1",
                            cardType = CardType.KB,
                            cardNo = "1111-2222-3333-4444",
                            amount = 1000L,
                            status = TransactionStatus.SUCCESS,
                            reason = null,
                        ),
                    ),
                )

            // when
            scheduler.reconcileOne(payment.id)

            // then
            val updated = paymentService.get(payment.id)
            assertThat(updated.status).isEqualTo(Payment.Status.SUCCESS)
        }

        @Test
        fun `PG가 실패면 결제, 주문을 실패처리 한다`() {
            // given
            val payment = saveProcessingPayment(orderId = 1L, transactionKey = "TRANSACTION_KEY")
            val order = mockOrder(payment.orderId, userId = 1L)

            given(pgClient.getTransaction(order.userId, "TRANSACTION_KEY"))
                .willReturn(
                    ApiResponse.success(
                        PgDto.TransactionDetailResponse(
                            transactionKey = "TRANSACTION_KEY",
                            orderId = "LOPPERS-1",
                            cardType = CardType.KB,
                            cardNo = "1111-2222-3333-4444",
                            amount = 1000L,
                            status = TransactionStatus.FAILED,
                            reason = "잔액부족이요",
                        ),
                    ),
                )

            // when
            scheduler.reconcileOne(payment.id)

            // then
            val updated = paymentService.get(payment.id)
            assertThat(updated.status).isEqualTo(Payment.Status.FAILED)
        }

        @Test
        fun `PG가 대기면 결제는 아무런 상태 변경을 하지 않고 프로세싱 상태이어야 한다`() {
            // given
            val payment = saveProcessingPayment(orderId = 1L, transactionKey = "TRANSACTION_KEY")
            val order = mockOrder(payment.orderId, userId = 1L)

            given(pgClient.getTransaction(order.userId, "TRANSACTION_KEY"))
                .willReturn(
                    ApiResponse.success(
                        PgDto.TransactionDetailResponse(
                            transactionKey = "TRANSACTION_KEY",
                            orderId = "LOPPERS-1",
                            cardType = CardType.KB,
                            cardNo = "1111-2222-3333-4444",
                            amount = 1000L,
                            status = TransactionStatus.PENDING,
                            reason = null,
                        ),
                    ),
                )

            // when
            scheduler.reconcileOne(payment.id)

            // then
            val updated = paymentService.get(payment.id)
            assertThat(updated.status).isEqualTo(Payment.Status.PROCESSING)
        }

        @Test
        fun `트랜잭션키가 없으면 주문 기반 조회로 키를 채워 넣는다`() {
            // given
            val payment = saveProcessingPayment(orderId = 1L, transactionKey = null)
            val order = mockOrder(payment.orderId, userId = 1L)

            given(pgClient.getOrder(order.userId, "LOOPERS-${order.id}"))
                .willReturn(
                    ApiResponse.success(
                        PgDto.OrderResponse(
                            orderId = "LOPPERS-1",
                            transactions = listOf(
                                PgDto.TransactionResponse(
                                    transactionKey = "TRANSACTION_KEY",
                                    status = TransactionStatus.PENDING,
                                    reason = null,
                                ),
                            ),
                        ),
                    ),
                )

            // when
            scheduler.reconcileOne(payment.id)

            // then
            val updated = paymentService.get(payment.id)
            assertThat(updated.transactionKey).isEqualTo("TRANSACTION_KEY")
            assertThat(updated.status).isEqualTo(Payment.Status.PROCESSING)
        }

        @Test
        fun `PROCESSING이 아니면 아무런 행위도 하지 않고 리턴한다`() {
            // given
            val payment = saveNonProcessingPayment(orderId = 1L)
            val order = mockOrder(payment.orderId, userId = 1L)

            // when
            scheduler.reconcileOne(payment.id)

            // then
            val reloaded = paymentService.get(payment.id)
            assertThat(reloaded.status).isEqualTo(Payment.Status.REQUESTED)
        }
    }
}
