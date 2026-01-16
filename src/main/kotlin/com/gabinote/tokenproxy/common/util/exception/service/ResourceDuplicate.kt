package com.gabinote.tokenproxy.common.util.exception.service

/**
 * 리소스 중복 예외 클래스
 */
class ResourceDuplicate(
    name: String,
    identifier: String,
    identifierType: String? = null,
) : ServiceException() {

    override val errorMessage: String = "$name already exists with identifier($identifierType): $identifier"


    override val logMessage: String = errorMessage

}