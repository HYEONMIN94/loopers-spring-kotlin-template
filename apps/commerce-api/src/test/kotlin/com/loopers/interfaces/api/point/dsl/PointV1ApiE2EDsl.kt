package com.loopers.interfaces.api.point.dsl

import com.loopers.domain.point.Point
import com.loopers.domain.user.User
import com.loopers.domain.user.User.Gender
import com.loopers.domain.user.User.Gender.MALE
import com.loopers.infrastructure.point.PointJpaRepository
import com.loopers.infrastructure.user.UserJpaRepository
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

object PointV1ApiE2EDsl {
    fun getHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
    }

    fun getHeaders(userName: String): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set("X-USER-ID", userName)
        }
    }

    fun saveUser(
        userRepository: UserJpaRepository,
        name: String = "userName",
        gender: Gender = MALE,
        birthDate: String = "1990-01-01",
        email: String = "xx@yy.zz",
    ): User {
        return userRepository.save(
            User.create(
                name,
                gender,
                birthDate,
                email,
            ),
        )
    }

    fun savePoint(
        pointRepository: PointJpaRepository,
        userId: Long = 1L,
        amount: Int = 1000,
    ): Point {
        return pointRepository.save(
            Point.create(
                userId,
                amount,
            ),
        )
    }
}
