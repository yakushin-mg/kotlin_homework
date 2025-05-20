import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Serializable
data class TickerResponse(val result: TickerResult)

@Serializable
data class TickerResult(
    @SerialName("last_price") val lastPrice: Double
)

class DeribitApiClient {
    private val client = HttpClient.newHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun fetchPrice(instrument: String): Double? {
        val url = "https://test.deribit.com/api/v2/public/ticker?instrument_name=$instrument"
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()

        return try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() != 200) return null
            val body = response.body()
            val parsed = json.decodeFromString<TickerResponse>(body)
            parsed.result.lastPrice
        } catch (e: Exception) {
            null
        }
    }
}
