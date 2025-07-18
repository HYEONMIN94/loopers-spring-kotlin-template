package com.loopers.interfaces.api.point

import com.loopers.domain.point.Point
import com.loopers.domain.user.User
import com.loopers.domain.user.User.Gender.MALE
import com.loopers.infrastructure.point.PointJpaRepository
import com.loopers.infrastructure.user.UserJpaRepository
import com.loopers.interfaces.api.ApiResponse
import com.loopers.utils.DatabaseCleanUp
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import kotlin.test.assertNotNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PointV1ApiE2ETest @Autowired constructor(
    private val testRestTemplate: TestRestTemplate,
    private val userJpaRepository: UserJpaRepository,
    private val pointRepository: PointJpaRepository,
    private val databaseCleanUp: DatabaseCleanUp,
) {
    companion object {
        private val ENDPOINT_GET_ME = "/api/v1/points"
        private val ENDPOINT_CHARGE = "/api/v1/points/charge"
    }

    @AfterEach
    fun tearDown() {
        databaseCleanUp.truncateAllTables()
    }

    @Nested
    inner class Get {
        @Test
        fun `포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다`() {
            // given
            val user = userJpaRepository.save(
                User.create(
                    "userName",
                    MALE,
                    "1990-01-01",
                    "xx@yy.zz",
                ),
            )

            val point = pointRepository.save(
                Point.create(user.id, 1000),
            )

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("X-USER-ID", user.userName.value)
            }

            // when
            val responseType = object : ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>>() {}
            val response = testRestTemplate.exchange(ENDPOINT_GET_ME, HttpMethod.GET, HttpEntity<Unit>(headers), responseType)

            // then
            assertAll(
                { assertThat(response.statusCode.is2xxSuccessful).isTrue() },
                { assertThat(response.body?.data?.userId).isEqualTo(point.userId) },
                { assertThat(response.body?.data?.amount).isEqualTo(point.amount.value) },
            )
        }

        @Test
        fun `X-USER-ID 헤더가 없을 경우, 400 Bad Request 응답을 반환한다`() {
            // given
            val user = userJpaRepository.save(
                User.create(
                    "userName",
                    MALE,
                    "1990-01-01",
                    "xx@yy.zz",
                ),
            )

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

            // when
            val responseType = object : ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>>() {}
            val response = testRestTemplate.exchange(ENDPOINT_GET_ME, HttpMethod.GET, HttpEntity<Unit>(headers), responseType)

            // then
            assertAll(
                { assert(response.statusCode.is4xxClientError) },
                { assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST) },
            )
        }
    }

    @Nested
    inner class Post {
        @Test
        fun `존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다`() {
            // given
            val user = userJpaRepository.save(
                User.create(
                    "userName",
                    MALE,
                    "1990-01-01",
                    "xx@yy.zz",
                ),
            )

            val point = pointRepository.save(
                Point.create(user.id, 1000),
            )

            val requestBody = PointV1Dto.ChargeRequest(1000)

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("X-USER-ID", user.userName.value)
            }

            val requestEntity = HttpEntity(requestBody, headers)

            // when
            val responseType = object : ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>>() {}
            val response = testRestTemplate.exchange(ENDPOINT_CHARGE, HttpMethod.POST, requestEntity, responseType)

            // then
            val totalAmount = point.amount.value + requestBody.amount
            assertAll(
                { assert(response.statusCode.is2xxSuccessful) },
                { assertNotNull(response.body?.data) },
                { assertThat(response.body?.data?.userId).isEqualTo(point.userId) },
                { assertThat(response.body?.data?.amount).isEqualTo(totalAmount) },
            )
        }

        @Test
        fun `존재하지 않는 유저로 요청할 경우, 404 Not Found 응답을 반환한다`() {
            // given
            val requestBody = PointV1Dto.ChargeRequest(1000)

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("X-USER-ID", "invalid")
            }

            val requestEntity = HttpEntity(requestBody, headers)

            // when
            val responseType = object : ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>>() {}
            val response = testRestTemplate.exchange(ENDPOINT_CHARGE, HttpMethod.POST, requestEntity, responseType)

            // then
            assertAll(
                { assert(response.statusCode.is4xxClientError) },
                { assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND) },
            )
        }
    }
}
