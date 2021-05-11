package com.yaroslavgamayunov.plugins

import FinnhubTrade
import com.example.Connection
import com.example.FinnhubWebsocketClient
import com.yaroslavgamayunov.SubscriptionRequest
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.utils.io.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.*
import java.util.*

private const val NOTIFICATION_PERIOD: Long = 5000

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
    val lastTrade = Collections.synchronizedMap<String, FinnhubTrade>(HashMap())

    val symbols = listOf("AAPL", "FB", "AMZN", "TSLA", "GOOG", "EBAY")
    val apiToken = "c0r43vv48v6qllqsate0"

    val finnhubClient = FinnhubWebsocketClient(symbols, apiToken)

    launch {
        finnhubClient.tradeDataFlow.collect {
            lastTrade[it.symbol] = it
        }
    }

    fun Connection.processRequest(request: SubscriptionRequest) {
        when (request) {
            is SubscriptionRequest.Subscribe -> subscriptions += request.symbols
            is SubscriptionRequest.Unsubscribe -> subscriptions -= request.symbols
        }
    }

    routing {
        webSocket("/stockviewer/trade/updates") {
            val connection = Connection(this)
            connections += connection

            launch {
                while (true) {
                    connection.sendTradeInfo(lastTrade.values.toList())
                    delay(NOTIFICATION_PERIOD)
                }
            }

            for (message in incoming) {
                message as? Frame.Text ?: continue
                try {
                    val request = Json.decodeFromString(SubscriptionRequest.serializer(), message.readText())
                    connection.processRequest(request)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
