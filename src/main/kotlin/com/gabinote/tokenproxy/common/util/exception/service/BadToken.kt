package com.gabinote.tokenproxy.common.util.exception.service

class BadToken(
    override val logMessage: String,
) : ServiceException()  {
    override val errorMessage: String = "The provided token is invalid or malformed."
}