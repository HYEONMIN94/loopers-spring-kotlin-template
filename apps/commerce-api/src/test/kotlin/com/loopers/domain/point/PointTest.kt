package com.loopers.domain.point

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class PointTest {
    @DisplayName("포인트 생성")
    @Nested
    inner class Charge {
        @ParameterizedTest
        @CsvSource(
            "0",
            "-1",
        )
        fun `0 이하의 정수로 포인트를 충전 시 실패한다`(amount: Int) {
            // when
            val result = assertThrows<CoreException> {
                Point.create(1L, amount)
            }

            // then
            assertThat(result.errorType).isEqualTo(ErrorType.BAD_REQUEST)
        }
    }
}
