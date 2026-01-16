package com.gabinote.tokenproxy.testSupport.testUtil.time

import com.gabinote.tokenproxy.common.util.time.TimeProvider
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class TestTimeProvider : TimeProvider {
    companion object {
        val testDateTime = LocalDateTime.of(2002, 8, 28, 0, 0)
        val testDate = testDateTime.toLocalDate()
        val testEpochSecond = testDateTime.toEpochSecond(ZoneOffset.ofHours(9))
        val testZoneOffset = ZoneOffset.ofHours(9)
    }

    override fun now(): LocalDateTime {
        ZoneId.of("UTC")
        return testDateTime
    }

    override fun today(): LocalDate {
        return testDate
    }

    override fun zoneId(): ZoneId {
        return ZoneId.of("Asia/Seoul")
    }

    override fun zoneOffset(): ZoneOffset {
        return ZoneOffset.ofHours(9)
    }
}