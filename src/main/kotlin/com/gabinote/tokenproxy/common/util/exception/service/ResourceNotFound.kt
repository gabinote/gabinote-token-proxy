package com.gabinote.tokenproxy.common.util.exception.service

class ResourceNotFound(
    val name: String,
    val identifier: String,
    val identifierType: String? = null,
) : ServiceException() {

    override val errorMessage: String = "$name not found with identifier($identifierType): $identifier"

    override val logMessage: String = errorMessage
}