package com.loopers.interfaces.api.ranking

import com.loopers.application.ranking.RankingFacade
import com.loopers.interfaces.api.ApiResponse
import com.loopers.interfaces.api.ranking.response.RankingV1Response.RankingsResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/rankings")
class RankingV1Controller(
    private val rankingFacade: RankingFacade,
) : RankingV1ApiSpec {
    @GetMapping
    override fun getRankings(
        @RequestParam date: String,
        @RequestParam size: Int,
        @RequestParam page: Int,
    ): ApiResponse<RankingsResponse> {
        return rankingFacade.getRankings(date, size, page)
            .let { RankingsResponse.from(it) }
            .let { ApiResponse.success(it) }
    }
}
