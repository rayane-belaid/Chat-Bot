package org.ldv.chatbot.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PageController {

    /*@GetMapping("/accueil")
    fun accueil(): String {
        return "accueil"
    }*/



        @GetMapping("/login")
        fun login(): String {
            return "auth/login"

        }
    }
