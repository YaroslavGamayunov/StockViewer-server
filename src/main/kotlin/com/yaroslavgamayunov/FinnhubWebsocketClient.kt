package com.example

import FinnhubTrade
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

class FinnhubWebsocketClient(stockSymbols: List<String>, private val apiToken: String) {
    private val httpClient by lazy {
        buildHttpClient()
    }

    val tradeDataFlow = flow {
        httpClient.webSocket(
            host = HOST,
            path = "?token=$apiToken"
        ) {
            stockSymbols.forEach { symbol ->
                val subscription = makeSubscriptionJson(symbol)
                outgoing.send(Frame.Text(Json.encodeToString(subscription)))
            }

            for (frame in incoming) {
                frame as? Frame.Text ?: continue
                try {
                    val frameJson = Json.parseToJsonElement(frame.readText())
                    val type = frameJson.jsonObject["type"]?.jsonPrimitive?.contentOrNull ?: continue
                    if (type != "trade") continue

                    val tradeData = Json.decodeFromJsonElement(
                        ListSerializer(FinnhubTrade.serializer()),
                        frameJson.jsonObject["data"]!!
                    )
                    tradeData.forEach { emit(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun makeSubscriptionJson(stockSymbol: String) = buildJsonObject {
        put("type", "subscribe")
        put("symbol", stockSymbol)
    }

    private fun buildHttpClient() = HttpClient {
        install(WebSockets)
        install(Logging)
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    companion object {
        private const val HOST = "ws.finnhub.io"
    }
}

