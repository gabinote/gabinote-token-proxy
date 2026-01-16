package com.gabinote.tokenproxy.common.util.exception.service

class ResourceQuotaLimit(
    val name: String,
    val quotaType: String,
    val quotaLimit: Long,
) : ServiceException() {

    override val errorMessage: String =
        "The resource quota limit for $name has been reached. The maximum allowed $quotaType is $quotaLimit."


    override val logMessage: String = errorMessage

}