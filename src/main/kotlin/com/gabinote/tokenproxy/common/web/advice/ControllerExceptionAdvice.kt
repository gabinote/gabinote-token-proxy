package com.gabinote.tokenproxy.common.web.advice

import com.gabinote.tokenproxy.common.util.exception.controller.RefreshCookieNotFound
import com.gabinote.tokenproxy.common.util.exception.service.BadToken
import com.gabinote.tokenproxy.common.util.exception.service.ServerError
import com.gabinote.tokenproxy.common.util.log.ErrorLog
import com.gabinote.tokenproxy.common.web.advice.ExceptionAdviceHelper.getRequestId
import com.gabinote.tokenproxy.common.web.advice.ExceptionAdviceHelper.problemDetail
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val logger = KotlinLogging.logger {}

/**
 * 컨트롤러 계층 예외 처리를 위한 어드바이스 클래스
 * 컨트롤러에서 발생하는 예외를 처리하는 최우선 어드바이스
 * @author 황준서
 */
@Order(1)
@RestControllerAdvice
class ControllerExceptionAdvice {

    @ExceptionHandler(RefreshCookieNotFound::class)
    fun handleRefreshCookieNotFound(
        ex: RefreshCookieNotFound,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val status = HttpStatus.UNAUTHORIZED

        val problemDetail = problemDetail(
            status = status,
            title = "Refresh Cookie Not Found",
            detail = ex.errorMessage,
            requestId = requestId
        )

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = status,
            error = "RefreshCookieNotFound",
            message = ex.logMessage
        )
        logger.error { log.toString() }
        return ResponseEntity(problemDetail, status)
    }
}