package com.gabinote.tokenproxy.common.util.exception.controller

/**
 * 리프레시 쿠키가 존재하지 않을 때 발생하는 예외
 */
class RefreshCookieNotFound : ControllerException() {

    override val errorMessage: String = "Refresh cookie not found. Please login again."

    override val logMessage: String = errorMessage
}