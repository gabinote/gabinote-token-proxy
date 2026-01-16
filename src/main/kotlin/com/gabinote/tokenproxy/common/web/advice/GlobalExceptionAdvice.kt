package com.gabinote.tokenproxy.common.web.advice

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.gabinote.tokenproxy.common.util.log.ErrorLog
import com.gabinote.tokenproxy.common.web.advice.ExceptionAdviceHelper.getRequestId
import com.gabinote.tokenproxy.common.web.advice.ExceptionAdviceHelper.problemDetail
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.core.annotation.Order
import org.springframework.core.convert.ConversionFailedException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.HandlerMethodValidationException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.net.URI

private val logger = KotlinLogging.logger {}

/**
 * 글로벌 예외 처리를 위한 어드바이스 클래스
 * Spring MVC에서 발생하는 다양한 예외를 처리하여 일관된 응답 포맷 제공
 * 모든 컨트롤러에 적용되는 마지막 예외 처리 계층
 * @author 황준서
 */
@Order(100)
@RestControllerAdvice
class GlobalExceptionAdvice {
    /**
     * 변환 실패 예외 처리
     * @param ex 발생한 예외
     * @param request HTTP 요청
     * @return 오류 응답
     */
    @ExceptionHandler(ConversionFailedException::class)
    fun handleConversionFailedException(
        ex: ConversionFailedException,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.BAD_REQUEST
        val detail = "Failed to convert value '${ex.value}' to type '${ex.targetType?.type}'"

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "ConversionFailedException",
            message = ex.message
        )
        logger.info { log }
        logger.debug(ex) { "Conversion failed stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Conversion Failed",
            detail = detail,
            instance = URI(request.requestURI),
            requestId = requestId
        )

        return ResponseEntity.status(httpStatus).body(pd)
    }

    /**
     * 메서드 인자 유효성 검사 실패 예외 처리
     * @param ex 발생한 예외
     * @param request HTTP 요청
     * @return 오류 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.BAD_REQUEST

        val clientDetails = ex.bindingResult.fieldErrors.map { it.defaultMessage ?: "Invalid value" }
        val serverDetails = ex.bindingResult.fieldErrors.map {
            "${it.field}: ${it.defaultMessage} (rejected value: ${it.rejectedValue})"
        }


        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "MethodArgumentNotValidException",
            message = serverDetails.joinToString("; ")
        )
        logger.info { log }
        logger.debug(ex) { "Validation stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Validation Failed",
            detail = "Validation failed for the request. See errors property for details.",
            instance = URI(request.requestURI),
            requestId = requestId,
            additionalProperties = mapOf("errors" to clientDetails)
        )

        return ResponseEntity.status(httpStatus).body(pd)
    }

    /**
     * 바인딩 예외 처리
     * @param ex 발생한 예외
     * @param request HTTP 요청
     * @return 오류 응답
     */
    @ExceptionHandler(BindException::class)
    fun handleBindException(
        ex: BindException,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.BAD_REQUEST

        val clientDetails = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        val serverDetails = ex.bindingResult.fieldErrors.map {
            "${it.field}: ${it.defaultMessage} (rejected value: ${it.rejectedValue})"
        }

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "BindException",
            message = serverDetails.joinToString("; ")
        )
        logger.info { log }
        logger.debug(ex) { "BindException stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Binding Failed",
            detail = "Request binding failed. See errors property for details.",
            instance = URI(request.requestURI),
            requestId = requestId,
            additionalProperties = mapOf("errors" to clientDetails)
        )

        return ResponseEntity.status(httpStatus).body(pd)
    }

    // --- Missing parameter ---
    /**
     * 필수 파라미터 누락 예외 처리
     * @param ex 발생한 예외
     * @param request HTTP 요청
     * @return 오류 응답
     */
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(
        ex: MissingServletRequestParameterException,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.BAD_REQUEST
        val detail = "Required parameter '${ex.parameterName}' of type '${ex.parameterType}' is missing"

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "MissingServletRequestParameterException",
            message = detail
        )
        logger.info { log }
        logger.debug(ex) { "Missing parameter stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Missing Required Parameter",
            detail = detail,
            instance = URI(request.requestURI),
            requestId = requestId
        )

        return ResponseEntity.status(httpStatus).body(pd)
    }

    // --- Type mismatch ---
    /**
     * 메서드 인자 타입 불일치 예외 처리
     * @param ex 발생한 예외
     * @param request HTTP 요청
     * @return 오류 응답
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.BAD_REQUEST
        val detail = "Parameter '${ex.name}' should be of type '${ex.requiredType?.simpleName}'"

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "MethodArgumentTypeMismatchException",
            message = ex.message
        )
        logger.info { log }
        logger.debug(ex) { "Type mismatch stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Type Mismatch",
            detail = detail,
            instance = URI(request.requestURI),
            requestId = requestId
        )

        return ResponseEntity.status(httpStatus).body(pd)
    }

    // --- Malformed JSON / 메시지 읽기 실패 ---
    /**
     * 잘못된 JSON 형식 또는 메시지 읽기 실패 예외 처리
     * @param ex 발생한 예외
     * @param request HTTP 요청
     * @return 오류 응답
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.BAD_REQUEST

        var detail = "Request body is not readable or malformed"
        val cause = ex.cause
        if (cause is MismatchedInputException) {
            val missingField = cause.path.joinToString(".") { it.fieldName }
            detail = "Missing or invalid required field: $missingField"
        }

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "HttpMessageNotReadableException",
            message = ex.message
        )
        logger.info { log }
        logger.debug(ex) { "Malformed body stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Malformed Request Body",
            detail = detail,
            instance = URI(request.requestURI),
            requestId = requestId
        )

        return ResponseEntity.status(httpStatus).body(pd)
    }

    // --- HTTP method not allowed ---
    /**
     * 지원하지 않는 HTTP 메서드 사용 예외 처리
     * @param ex 발생한 예외
     * @param request HTTP 요청
     * @return 오류 응답
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(
        ex: HttpRequestMethodNotSupportedException,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.METHOD_NOT_ALLOWED
        val detail = "Method '${ex.method}' is not supported for this endpoint. Supported methods: ${
            ex.supportedMethods?.joinToString(", ")
        }"

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "HttpRequestMethodNotSupportedException",
            message = ex.message
        )
        logger.info { log }
        logger.debug(ex) { "Method not allowed stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Method Not Allowed",
            detail = detail,
            instance = URI(request.requestURI),
            requestId = requestId
        )

        return ResponseEntity.status(httpStatus).body(pd)
    }

    // --- Unsupported media type ---
    /**
     * 지원하지 않는 미디어 타입 예외 처리
     * @param ex 발생한 예외
     * @param request HTTP 요청
     * @return 오류 응답
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupportedException(
        ex: HttpMediaTypeNotSupportedException,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.UNSUPPORTED_MEDIA_TYPE
        val detail = "Media type '${ex.contentType}' is not supported. Supported media types: ${
            ex.supportedMediaTypes.joinToString(", ")
        }"

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "HttpMediaTypeNotSupportedException",
            message = ex.message
        )
        logger.info { log }
        logger.debug(ex) { "Unsupported media type stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Unsupported Media Type",
            detail = detail,
            instance = URI(request.requestURI),
            requestId = requestId
        )

        return ResponseEntity.status(httpStatus).body(pd)
    }

    // --- No resource / handler found ---
    /**
     * 리소스 또는 핸들러를 찾을 수 없는 경우 예외 처리
     * @param ex 발생한 예외
     * @param request HTTP 요청
     * @return 오류 응답
     */
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(
        ex: NoResourceFoundException,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.NOT_FOUND
        val detail = "No resource found for ${ex.resourcePath}"

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "NoResourceFoundException",
            message = ex.message
        )
        logger.info { log }
        logger.debug(ex) { "No resource found stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Resource Not Found",
            detail = detail,
            instance = URI(request.requestURI),
            requestId = requestId
        )

        return ResponseEntity.status(httpStatus).body(pd)
    }

    /**
     * 핸들러를 찾을 수 없는 경우 예외 처리
     * @param ex 발생한 예외
     * @param request HTTP 요청
     * @return 오류 응답
     */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(
        ex: NoHandlerFoundException,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.NOT_FOUND
        val detail = "No handler found for ${ex.httpMethod} ${ex.requestURL}"

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = "NoHandlerFoundException",
            message = ex.message
        )
        logger.info { log }
        logger.debug(ex) { "No handler found stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Handler Not Found",
            detail = detail,
            instance = URI(request.requestURI),
            requestId = requestId
        )

        return ResponseEntity.status(httpStatus).body(pd)
    }

    // --- Method-level validation exceptions (ConstraintViolation) ---
    /**
     * 메서드 수준 유효성 검사 예외 처리
     * @param ex 발생한 예외
     * @param request HTTP 요청
     * @return 오류 응답
     */
    @ExceptionHandler(HandlerMethodValidationException::class, ConstraintViolationException::class)
    fun handleValidationExceptions(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.BAD_REQUEST

        val clientDetails = when (ex) {
            is ConstraintViolationException -> ex.constraintViolations.map { it.message ?: "Invalid value" }
            is HandlerMethodValidationException -> (ex as? ConstraintViolationException)?.constraintViolations?.map {
                it.message
                    ?: "Invalid value"
            } ?: listOf("Invalid value")

            else -> listOf("Invalid value")
        }

        val serverDetails = clientDetails // 간단화

        val log = ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = ex::class.simpleName ?: "ValidationException",
            message = serverDetails.joinToString("; ")
        )
        logger.info { log }
        logger.debug(ex) { "Validation stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Validation Failed",
            detail = "Validation failed. See errors property for details.",
            instance = URI(request.requestURI),
            requestId = requestId,
            additionalProperties = mapOf("errors" to clientDetails)
        )

        return ResponseEntity.status(httpStatus).body(pd)
    }

    // --- Fallback: 모든 기타 예외 처리 ---
    /**
     * 모든 기타 예외 처리
     * @param ex 발생한 예외
     * @param request HTTP 요청
     * @return 오류 응답
     */
    @ExceptionHandler(Exception::class)
    fun handleException(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ProblemDetail> {
        val requestId = getRequestId(request)
        val httpStatus = HttpStatus.INTERNAL_SERVER_ERROR

        ErrorLog(
            requestId = requestId,
            method = request.method,
            path = request.requestURI,
            status = httpStatus,
            error = ex::class.simpleName ?: "Exception",
            message = ex.message
        )
        logger.error { "Unhandled Exception [requestId=$requestId]: ${ex.message ?: ""}" }
        logger.debug(ex) { "Unhandled Exception stacktrace [requestId=$requestId]" }

        val pd = problemDetail(
            status = httpStatus,
            title = "Internal Server Error",
            detail = "An unexpected error occurred. (ref: $requestId)",
            instance = URI(request.requestURI),
            requestId = requestId
        )

        return ResponseEntity.status(httpStatus).body(pd)
    }

}