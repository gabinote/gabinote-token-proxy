package com.gabinote.tokenproxy.common.util.exception.service

class ResourceNotValid(
    val name: String,
    val reasons: List<String>,
) : ServiceException() {

    override val errorMessage: String = "$name is not valid"
    override val logMessage: String = "$name is not valid. $errorMessage: ${reasons.joinToString(", ")}"

}