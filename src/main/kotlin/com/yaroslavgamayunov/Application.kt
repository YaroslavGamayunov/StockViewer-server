package com.yaroslavgamayunov

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.yaroslavgamayunov.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureSockets()
        configureSerialization()
        configureMonitoring()
    }.start(wait = true)
}
