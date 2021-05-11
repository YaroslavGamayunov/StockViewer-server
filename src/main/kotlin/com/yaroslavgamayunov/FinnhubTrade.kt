import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FinnhubTrade(
    @SerialName("c") val tradeConditions: List<String>,
    @SerialName("p") val price: Double,
    @SerialName("s") val symbol: String,
    @SerialName("t") val time: Long,
    @SerialName("v") val volume: Long
)
