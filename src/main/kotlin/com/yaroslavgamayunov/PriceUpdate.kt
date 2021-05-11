package com.example

import kotlinx.serialization.Serializable

@Serializable
data class PriceUpdate(
    val price: Double,
    val time: Long,
    val symbol: String
)