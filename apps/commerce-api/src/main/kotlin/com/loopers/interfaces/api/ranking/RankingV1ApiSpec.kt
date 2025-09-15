package com.loopers.interfaces.api.ranking

import com.loopers.interfaces.api.ApiResponse
import com.loopers.interfaces.api.ranking.response.RankingV1Response.RankingsResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Ranking V1 API", description = "Loopers 랭킹 API 입니다.")
interface RankingV1ApiSpec {
    @Operation(
        summary = "랭킹 목록 조회",
        description = "랭킹 목록을 조회합니다.",
    )
    fun getRankings(
        @RequestParam date: String,
        @RequestParam size: Int,
        @RequestParam page: Int,
    ): ApiResponse<RankingsResponse>
}
