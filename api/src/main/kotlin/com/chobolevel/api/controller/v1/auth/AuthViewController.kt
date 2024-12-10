package com.chobolevel.api.controller.v1.auth

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.GetMapping

@Controller
class AuthViewController {

    @GetMapping("/login")
    fun login(model: Model): String {
        model["test"] = "123"
        return "login"
    }

    @GetMapping("/authorize")
    fun authorize(): String {
        return "authorize"
    }
}
