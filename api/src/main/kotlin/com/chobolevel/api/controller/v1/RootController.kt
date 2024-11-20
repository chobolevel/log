package com.chobolevel.api.controller.v1

import io.swagger.v3.oas.annotations.Hidden
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Hidden
@RestController
class RootController {

    @GetMapping("/")
    fun home(): String {
        return "CHOLO - \uD83D\uDCBB초보 개발자의 블로그\uD83D\uDCBB API"
    }
}
