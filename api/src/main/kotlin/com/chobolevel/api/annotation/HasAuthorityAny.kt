package com.chobolevel.api.annotation

import org.springframework.security.access.prepost.PreAuthorize

@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
annotation class HasAuthorityAny()
