package com.loopers.domain.point

import com.loopers.application.point.PointFacade
import com.loopers.application.point.PointInfo
import com.loopers.domain.user.User
import com.loopers.domain.user.User.Gender.MALE
import com.loopers.infrastructure.point.PointJpaRepository
import com.loopers.infrastructure.user.UserJpaRepository
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import com.loopers.utils.DatabaseCleanUp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PointServiceIntegrationTest @Autowired constructor(
    private val pointFacade: PointFacade,
    private val pointService: PointService,
    private val pointRepository: PointJpaRepository,
    private val userRepository: UserJpaRepository,
    private val userService: UserJpaRepository,
    private val databaseCleanUp: DatabaseCleanUp,
) {

    @AfterEach
    fun tearDown() {
        databaseCleanUp.truncateAllTables()
    }

    @Nested
    inner class Get {
        @Test
        fun `해당 ID의 회원이 존재할 경우, 보유 포인트가 반환된다`() {
            // given
            val user = userRepository.save(
                User.create(
                    userName = "userName",
                    gender = User.Gender.MALE,
                    birthDate = "1990-01-01",
                    email = "tester@example.com",
                ),
            )

            // when
            val result = pointFacade.getMe(user.id)

            // then
            assertThat(result).isNotNull()
            assertThat(result?.amount).isEqualTo(0)
        }

        @Test
        fun `해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다`() {
            // given
            val userId = -1L

            // when
            val result = pointService.getMe(userId)

            // then
            assertThat(result).isNull()
        }
    }

    @Nested
    inner class Charge {
        @Test
        fun `존재하지_않는 유저 ID 로 충전을 시도한 경우, 실패한다`() {
            // given
            val userName = "invalid"
            val amount = 1000

            // when & then
            val exception = assertThrows<CoreException> {
                pointFacade.charge(PointInfo.Charge.of(userName, amount))
            }

            // then
            assertThat(exception.errorType).isEqualTo(ErrorType.NOT_FOUND)
        }
    }
}
