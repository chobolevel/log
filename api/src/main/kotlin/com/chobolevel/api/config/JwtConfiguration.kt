package com.chobolevel.api.config

import com.chobolevel.api.properties.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(value = [JwtProperties::class])
class JwtConfiguration
