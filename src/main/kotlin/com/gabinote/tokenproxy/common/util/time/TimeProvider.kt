package com.gabinote.tokenproxy.common.util.time

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

interface TimeProvider {
    /**
     * 현재 날짜와 시간을 반환
     * @return 현재 LocalDateTime
     */
    fun now(): LocalDateTime

    /**
     * 현재 날짜를 반환
     * @return 현재 LocalDate
     */
    fun today(): LocalDate

    fun zoneId(): ZoneId

    fun zoneOffset(): ZoneOffset
}