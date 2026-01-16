package com.gabinote.tokenproxy.testSupport.testConfig.kotest

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension
import io.kotest.extensions.spring.SpringExtension

class KotestConfig : AbstractProjectConfig() {

//    override fun extensions(): List<Extension> = listOf(SpringTestContextModeExtension())
    override fun extensions(): List<Extension> = listOf(SpringExtension)

}