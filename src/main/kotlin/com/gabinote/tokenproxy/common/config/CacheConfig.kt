package com.gabinote.tokenproxy.common.config

import com.gabinote.tokenproxy.common.config.cache.CacheDefine
import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.support.SimpleCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@EnableCaching
@Configuration
class CacheConfig {

    fun getCaffeineConfigs(): List<CaffeineCache> {
        val caches = mutableListOf<CaffeineCache>()
        CacheDefine.entries.forEach { cacheDefine ->
            val cache = CaffeineCache(
                cacheDefine.cacheName,
                Caffeine.newBuilder()
                    .expireAfterWrite(cacheDefine.expireAfterWriteMinutes, TimeUnit.MINUTES)
                    .maximumSize(cacheDefine.maximumSize)
                    .build()
            )
            caches.add(cache)
        }
        return caches
    }

    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = SimpleCacheManager()
        val configs = getCaffeineConfigs()
        cacheManager.setCaches(configs)
        return cacheManager
    }
}