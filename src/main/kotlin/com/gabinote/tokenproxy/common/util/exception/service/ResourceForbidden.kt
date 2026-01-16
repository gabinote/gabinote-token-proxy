package com.gabinote.tokenproxy.common.util.exception.service

class ResourceForbidden(
    name: String,
    act: String,
    blockedUser: String,
) : ServiceException() {

    override val errorMessage: String = "$blockedUser is forbidden to $act on $name"
    override val logMessage: String = errorMessage

}