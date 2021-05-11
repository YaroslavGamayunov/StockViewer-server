package com.example

import FinnhubTrade
import io.ktor.http.cio.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

data class Connection(val session: DefaultWebSocketSession) {
    companion object {
        var lastId = AtomicInteger(0)
    }

    val name = "user${lastId.getAndIncrement()}"

    val subscriptions: MutableSet<String> = Collections.synchronizedSet(LinkedHashSet())
    private val updateTime: MutableMap<String, Long> = mutableMapOf()

    suspend fun sendTradeInfo(finnhubTrades: List<FinnhubTrade>) {
        finnhubTrades
            .filter { it.symbol in subscriptions && updateTime.getOrDefault(it.symbol, 0) < it.time }
            .forEach {
                val update = PriceUpdate(it.price, it.time, it.symbol)
                updateTime[it.symbol] = it.time

                session.outgoing.send(Frame.Text(Json.encodeToString(update)))
            }
    }
}
