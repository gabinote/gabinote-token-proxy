package com.gabinote.tokenproxy.common.util.exception.service

class ResourceNotOwner(
    name: String,
    notOwner: String,
) : ServiceException() {

    override val errorMessage: String = "$notOwner is not the owner of $name"
    override val logMessage: String = errorMessage

}