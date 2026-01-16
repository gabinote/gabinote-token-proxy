package com.gabinote.tokenproxy.common.config.cache

enum class CacheDefine(
    val cacheName: String,
    val expireAfterWriteMinutes: Long = 10L,
    val maximumSize: Long = 1000L,
) {

}