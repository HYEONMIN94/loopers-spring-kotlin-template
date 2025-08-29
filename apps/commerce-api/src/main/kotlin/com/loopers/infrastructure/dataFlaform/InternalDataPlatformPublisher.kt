package com.loopers.infrastructure.dataFlaform

import org.springframework.stereotype.Component

@Component
class InternalDataPlatformPublisher() : DataFlatformPublisher {
    override fun publish(event: Any) {
        // 외부 데이터 플랫폼에 전송
    }
}
