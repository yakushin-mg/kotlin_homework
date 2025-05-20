import kotlinx.coroutines.*
import java.time.Instant

data class UserSettings(
    val instrument: String,
    val windowSeconds: Int,
    val method: String
)

class CryptoApp {
    private val api = DeribitApiClient()
    private val analyzer = PriceAnalyzer()
    private val coins = listOf("BTC-PERPETUAL", "ETH-PERPETUAL")
    private val methods = listOf("Линейная экстраполяция", "Средняя цена")
    private val inputHandler = ConsoleInput(coins, methods)
    private val outputHandler = ConsoleOutput()

    suspend fun process() = coroutineScope {
        do {
            val settings = inputHandler.readUserSettings()
            outputHandler.printStartInfo(settings)

            val history = mutableListOf<PricePoint>()
            for (i in 1..settings.windowSeconds) {
                val now = Instant.now().toEpochMilli()
                val price = api.fetchPrice(settings.instrument)
                if (price != null) {
                    history.add(PricePoint(now, price))
                    val result = when (settings.method) {
                        "Средняя цена" -> analyzer.average(history)
                        "Линейная экстраполяция" -> analyzer.linearExtrapolation(history)
                        else -> 0.0
                    }
                    outputHandler.print(settings.method, i, settings.windowSeconds, price, result)
                } else {
                    outputHandler.printError(i, settings.windowSeconds)
                }
                delay(1000)
            }
        } while (inputHandler.needRepeatQuery())
        println("До свидания! Желаю удачи!")
    }
}
