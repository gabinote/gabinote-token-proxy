package com.gabinote.tokenproxy.common.util.exception.controller

/**
 * 게이트웨이 인증 실패 예외 클래스
 */
class GatewayAuthFailed : ControllerException() {

    override val errorMessage: String = "Gateway authentication failed"

    override val logMessage: String = errorMessage
}