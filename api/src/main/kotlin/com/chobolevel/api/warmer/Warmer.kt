package com.chobolevel.api.warmer

interface Warmer {

    suspend fun run()

    var isDone: Boolean
}
