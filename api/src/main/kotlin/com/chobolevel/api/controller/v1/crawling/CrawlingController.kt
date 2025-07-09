package com.chobolevel.api.controller.v1.crawling

import com.chobolevel.api.dto.common.ResultResponse
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class CrawlingController {

    @GetMapping("/crawling")
    fun crawling(@RequestParam url: String, @RequestParam classNames: List<String>): ResponseEntity<ResultResponse> {
        val doc: Document = Jsoup.connect(url).get()
        val result: Elements = doc.getElementsByClass(classNames.joinToString(", "))
        return ResponseEntity.ok(ResultResponse(result.map { it.text() }))
    }
}
