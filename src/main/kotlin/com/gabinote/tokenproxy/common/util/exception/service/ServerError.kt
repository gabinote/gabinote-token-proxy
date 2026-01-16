package com.gabinote.tokenproxy.common.util.exception.service

class ServerError(
    val reason: String,
) : ServiceException() {

    override val errorMessage: String = reason


    override val logMessage: String = errorMessage

}