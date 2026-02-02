package com.gabinote.tokenproxy.common.web.advice

import com.gabinote.tokenproxy.common.util.exception.service.BadToken
import com.gabinote.tokenproxy.common.util.exception.service.ResourceDuplicate
import com.gabinote.tokenproxy.common.util.exception.service.ResourceNotFound
import com.gabinote.tokenproxy.common.util.exception.service.ResourceNotValid
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
 * 서비스 계층 예외 처리를 위한 어드바이스 클래스
 * 서비스 계층에서 발생하는 비즈니스 예외를 처리
 * @author 황준서
 */
@Order(1)
@RestControllerAdvice
class ServiceExceptionAdvice {

    /**
     * 리소스를 찾을 수 없는 예외 처리
     * @param ex 발생한 예외
     * @param request HTTP 요청
     * @return 오류 응답
     */
    @ExceptionHandler(ResourceNotFound::class)
    fun handleResourceNotFound(
        ex: ResourceNotFound,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val status = HttpStatus.NOT_FOUND
        val problemDetail = problemDetail(
            status = status,
            title = "Resource Not Found",
            detail = ex.errorMessage,
            requestId = requestId
        )
        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = status,
            error = "ResourceNotFound",
            message = ex.errorMessage
        )

        logger.info { log.toString() }
        return ResponseEntity(problemDetail, status)
    }

    /**
     * 리소스 중복 예외 처리
     * @param ex 발생한 예외
     * @param request HTTP 요청
     * @return 오류 응답
     */
    @ExceptionHandler(ResourceDuplicate::class)
    fun handleResourceDuplicate(
        ex: ResourceDuplicate,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val status = HttpStatus.CONFLICT
        val problemDetail = problemDetail(
            status = status,
            title = "Resource Duplicate",
            detail = ex.message,
            requestId = requestId
        )
        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = status,
            error = "ResourceDuplicate",
            message = ex.message
        )
        logger.info { log.toString() }
        return ResponseEntity(problemDetail, status)
    }

    /**
     * 리소스 유효성 검사 실패 예외 처리
     * @param ex 발생한 예외
     * @param request HTTP 요청
     * @return 오류 응답
     */
    @ExceptionHandler(ResourceNotValid::class)
    fun handleResourceNotValid(
        ex: ResourceNotValid,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val status = HttpStatus.BAD_REQUEST

        val problemDetail = problemDetail(
            status = status,
            title = "Resource Not Valid",
            detail = ex.errorMessage,
            requestId = requestId
        )

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = status,
            error = "ResourceNotValid",
            message = ex.logMessage
        )
        logger.info { log.toString() }
        return ResponseEntity(problemDetail, status)
    }

    @ExceptionHandler(ServerError::class)
    fun handleServerError(
        ex: ServerError,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val status = HttpStatus.INTERNAL_SERVER_ERROR

        val problemDetail = problemDetail(
            status = status,
            title = "Server Error",
            detail = ex.errorMessage,
            requestId = requestId
        )

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = status,
            error = "ServerError",
            message = ex.logMessage
        )
        logger.error { log.toString() }
        return ResponseEntity(problemDetail, status)
    }

    @ExceptionHandler(BadToken::class)
    fun handleBadToken(
        ex: BadToken,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val status = HttpStatus.UNAUTHORIZED

        val problemDetail = problemDetail(
            status = status,
            title = "Bad Token",
            detail = ex.errorMessage,
            requestId = requestId
        )

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = status,
            error = "BadToken",
            message = ex.logMessage
        )
        logger.error { log.toString() }
        return ResponseEntity(problemDetail, status)
    }

}