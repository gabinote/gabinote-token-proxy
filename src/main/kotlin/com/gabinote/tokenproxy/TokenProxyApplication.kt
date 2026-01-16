package com.gabinote.tokenproxy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TokenProxyApplication

fun main(args: Array<String>) {
    runApplication<TokenProxyApplication>(*args)
}
