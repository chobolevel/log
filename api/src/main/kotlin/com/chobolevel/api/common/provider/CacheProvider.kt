package com.chobolevel.api.common.provider

import java.util.concurrent.TimeUnit

interface CacheProvider {

    fun get(key: String): String?

    fun put(key: String, value: String)

    fun put(key: String, value: String, duration: Long, unit: TimeUnit)

    fun delete(key: String)
}
