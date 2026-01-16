package com.gabinote.tokenproxy.common.util.log

import org.springframework.http.HttpStatus
import java.time.Instant

data class ErrorLog(
    val timestamp: Instant = Instant.now(),
    val requestId: String,
    val method: String,
    val path: String,
    val status: HttpStatus,
    val error: String,
    val message: String?
) {
    override fun toString(): String {
        return "ErrorLog(timestamp=$timestamp, requestId='$requestId', method='$method', path='$path', status=$status, error='$error', message=$message)"
    }
}