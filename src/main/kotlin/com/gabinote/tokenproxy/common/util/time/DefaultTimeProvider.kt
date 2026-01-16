package com.gabinote.tokenproxy.common.util.time

import java.time.*

class DefaultTimeProvider(
    private val clock: Clock,
) : TimeProvider {
    /**
     * 현재 날짜와 시간을 반환
     * @return 현재 LocalDateTime
     */
    override fun now(): LocalDateTime {
        return LocalDateTime.now(clock)
    }

    /**
     * 현재 날짜를 반환
     * @return 현재 LocalDate
     */
    override fun today(): LocalDate {
        return LocalDate.now(clock)
    }

    override fun zoneId(): ZoneId {
        return clock.zone
    }

    override fun zoneOffset(): ZoneOffset {
        return clock.zone.rules.getOffset(clock.instant())
    }
}