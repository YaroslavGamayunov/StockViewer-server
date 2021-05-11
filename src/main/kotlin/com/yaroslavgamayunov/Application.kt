package com.yaroslavgamayunov

import com.yaroslavgamayunov.plugins.configureMonitoring
import com.yaroslavgamayunov.plugins.configureRouting
import com.yaroslavgamayunov.plugins.configureSerialization
import com.yaroslavgamayunov.plugins.configureSockets
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureSockets()
        configureSerialization()
        configureMonitoring()
    }.start(wait = true)
}
