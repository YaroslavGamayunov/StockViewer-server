package com.yaroslavgamayunov

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class SubscriptionRequest {
    @Serializable
    @SerialName("subscribe")
    data class Subscribe(val symbols: List<String>) : SubscriptionRequest()

    @Serializable
    @SerialName("unsubscribe")
    class Unsubscribe(val symbols: List<String>) : SubscriptionRequest()
}