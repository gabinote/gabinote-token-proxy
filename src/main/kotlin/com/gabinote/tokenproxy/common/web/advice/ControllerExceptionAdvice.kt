package com.gabinote.tokenproxy.common.web.advice

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.annotation.Order
import org.springframework.web.bind.annotation.RestControllerAdvice

private val logger = KotlinLogging.logger {}

/**
 * 컨트롤러 계층 예외 처리를 위한 어드바이스 클래스
 * 컨트롤러에서 발생하는 예외를 처리하는 최우선 어드바이스
 * @author 황준서
 */
@Order(1)
@RestControllerAdvice
class ControllerExceptionAdvice