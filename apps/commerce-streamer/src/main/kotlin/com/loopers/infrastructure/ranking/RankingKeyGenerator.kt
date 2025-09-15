package com.loopers.infrastructure.ranking

import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class RankingKeyGenerator {
    fun keyFor(date: LocalDate = LocalDate.now()): String =
        "ranking:all:${date.format(DateTimeFormatter.BASIC_ISO_DATE)}"
}
