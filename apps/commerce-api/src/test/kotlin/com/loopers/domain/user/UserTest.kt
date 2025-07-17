package com.loopers.domain.user

import com.loopers.support.error.CoreException
import com.loopers.support.error.ErrorType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class UserTest {
    @DisplayName("유저 생성")
    @Nested
    inner class Create {
        @ParameterizedTest
        @CsvSource(
            "a한글이껴있네1A",
            "한글이야",
            "''",
            "12345678910"
        )
        fun `ID가 형식에 맞지 않으면 객체 생성에 실패한다`(userName: String) {
            // when
            val result = assertThrows<CoreException> {
                User.create(userName, User.Gender.MALE, "1990-01-01", "email@ema.il")
            }

            // then
            assertThat(result.errorType).isEqualTo(ErrorType.BAD_REQUEST)
        }

        @ParameterizedTest
        @CsvSource(
            "xxyy.zz",
            "xx@yyzz",
            "xx@@yy.zz",
            "@yy.zz",
            "xx@.zz",
            "xx@yy.",
        )
        fun `이메일이 형식에 맞지 않으면 객체 생성에 실패한다`(email : String) {
            // when
            val result = assertThrows<CoreException> {
                User.create("userName", User.Gender.MALE, "1990-01-01", email)
            }

            // then
            assertThat(result.errorType).isEqualTo(ErrorType.BAD_REQUEST)
        }

        @ParameterizedTest
        @CsvSource(
            "999-99-99-99",
            "999999-99",
            "9999-9999",
            "99999999",
            "9999-999-99",
            "9999-99-909",
        )
        fun `생년월일이 형식에 맞지 않으면 객체 생성에 실패한다`(birthDate : String) {
            // when
            val result = assertThrows<CoreException> {
                User.create("userName", User.Gender.MALE, birthDate, "email@ema.il")
            }

            // then
            assertThat(result.errorType).isEqualTo(ErrorType.BAD_REQUEST)
        }
    }
}
