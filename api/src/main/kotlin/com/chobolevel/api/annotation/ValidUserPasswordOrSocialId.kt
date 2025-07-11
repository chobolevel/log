package com.chobolevel.api.annotation

import com.chobolevel.api.service.user.validator.UserPasswordOrSocialIdValidator
import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [UserPasswordOrSocialIdValidator::class])
annotation class ValidUserPasswordOrSocialId(
    val message: String = "비밀번호는 필수 값입니다.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)
