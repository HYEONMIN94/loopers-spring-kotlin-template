package com.loopers.domain.user

import com.loopers.application.user.UserFacade
import com.loopers.application.user.UserInfo
import com.loopers.domain.user.User.Gender.MALE
import com.loopers.infrastructure.user.UserJpaRepository
import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import com.loopers.utils.DatabaseCleanUp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class UserServiceIntegrationTest @Autowired constructor(
    private val userFacade: UserFacade,
    private val userService: UserService,
    private val userRepository: UserJpaRepository,
    private val databaseCleanUp: DatabaseCleanUp,
) {

    @AfterEach
    fun tearDown() {
        databaseCleanUp.truncateAllTables()
    }

    @DisplayName("조회")
    @Nested
    inner class Get {
        @Test
        fun `해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다`() {
            // given
            val exampleModel = userRepository.save(
                User.create(
                    "userId",
                    MALE,
                    "1990-01-01",
                    "xx@yy.zz",
                ),
            )

            // when
            val result = userService.getMe(exampleModel.userName.value)

            // then
            assertAll(
                { assertThat(result).isNotNull() },
                { assertThat(result?.id).isEqualTo(exampleModel.id) },
                { assertThat(result?.userName).isEqualTo(exampleModel.userName) },
                { assertThat(result?.gender).isEqualTo(exampleModel.gender) },
                { assertThat(result?.birthDate).isEqualTo(exampleModel.birthDate) },
                { assertThat(result?.email).isEqualTo(exampleModel.email) },
            )

        }

        @Test
        fun `해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다`() {
            // given
            val userName = "userName"

            // when
            val result = userService.getMe("userName")

            // then
            assertThat(result).isNull()
        }
    }

    @DisplayName("회원가입")
    @Nested
    inner class SignUp {
        @Test
        fun `회원 가입 시 user 정보를 저장한다`() {
            // when
            userFacade.signUp(
                UserInfo.SignUp(
                    "userName",
                    MALE,
                    "1990-01-01",
                    "xx@yy.zz",
                ),
            )

            // then
            val user = userRepository.findAll()
            assertThat(user.size).isEqualTo(1)
        }

        @Test
        fun `이미 가입된 ID 로 회원가입 시도 시 실패한다`() {
            // given
            val userName = "userName"
            val userInfo = UserInfo.SignUp(userName, MALE, "1990-01-01", "email@domain.com")
            userFacade.signUp(userInfo)

            // when
            val exception = assertThrows<CoreException> {
                userFacade.signUp(userInfo)
            }

            // then
            assertThat(exception.errorType).isEqualTo(ErrorType.CONFLICT)
        }
    }
}
