import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun main() {
    val app = WeatherApp()
    app.process()
}

data class Coordinates(
    var latitude: Double,
    var longitude: Double
)

data class DatesRange(
    var start: LocalDate,
    var end: LocalDate
)

data class MinTemp(
    val value: Double,
    val date: LocalDate
)

data class MaxTemp(
    val value: Double,
    val date: LocalDate
)

data class MaxTempDiff(
    val value: Double,
    val date: LocalDate
)

data class WeatherResponse(
    val coordinates: Coordinates,
    val datesRange: DatesRange,
    val minTemp: MinTemp,
    val maxTemp: MaxTemp,
    val maxTempDiff: MaxTempDiff,
    val avgTempValue: Double
)

class WeatherCache {
    private val cache = mutableListOf<WeatherResponse>()

    fun addToCache(weatherData: WeatherResponse) {
        cache.add(weatherData)
    }

    fun find(coords: Coordinates, dates: DatesRange): WeatherResponse? {
        return cache.firstOrNull { data ->
            data.coordinates.latitude == coords.latitude &&
                    data.coordinates.longitude == coords.longitude &&
                    data.datesRange.start == dates.start &&
                    data.datesRange.end == dates.end
        }
    }
}


class ConsoleInput {
    companion object {
        fun getCoords(): Coordinates {
            while (true) {
                try {
                    print("Введите широту (между -90 и 90): ")
                    val latitude = readLine()?.toDoubleOrNull() ?: throw IllegalArgumentException("Введите число")
                    if (latitude < -90 || latitude > 90) throw IllegalArgumentException("Введите число в промежутке от -90 по 90")

                    print("Введите долготу (между -180 и 180): ")
                    val longitude = readLine()?.toDoubleOrNull() ?: throw IllegalArgumentException("Введите число")
                    if (longitude < -180 || longitude > 180) throw IllegalArgumentException("Введите число в промежутке от -180 по 180")

                    return Coordinates(latitude, longitude)
                } catch (e: Exception) {
                    println("Ошибка: ${e.message}")
                }
            }
        }

        fun getDatesRange(): DatesRange {
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            while (true) {
                try {
                    print("Введите начальную дату (ГГГГ-ММ-ДД): ")
                    val startDateStr = readLine() ?: throw IllegalArgumentException("Необходимо ввести дату")
                    val start = try {
                        LocalDate.parse(startDateStr, dateFormatter)
                    } catch (e: DateTimeParseException) {
                        throw IllegalArgumentException("Некорректный формат даты. Используйте ГГГГ-ММ-ДД")
                    }

                    print("Введите конечную дату (ГГГГ-ММ-ДД): ")
                    val endDateStr = readLine() ?: throw IllegalArgumentException("Необходимо ввести дату")
                    val end = try {
                        LocalDate.parse(endDateStr, dateFormatter)
                    } catch (e: DateTimeParseException) {
                        throw IllegalArgumentException("Некорректный формат даты. Используйте ГГГГ-ММ-ДД")
                    }

                    if (end.isBefore(start)) {
                        throw IllegalArgumentException("Конечная дата должна быть позже начальной")
                    }

                    val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(start, end)
                    if (daysBetween > 365) {
                        throw IllegalArgumentException("Слишком большой диапазон. Максимум 1 год (365 дней)")
                    }

                    return DatesRange(start, end)
                } catch (e: Exception) {
                    println("Ошибка: ${e.message}")
                }
            }
        }
    }

}

class ConsoleOutput {
    companion object {
        fun printResponse(response: WeatherResponse) {
            println("""
        
            🌤️ ПОГОДНЫЙ ОТЧЕТ 🌤️
                ⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯
                📍 Локация: ${response.coordinates.latitude}°N, ${response.coordinates.longitude}°E
                📅 Период: ${response.datesRange.start} — ${response.datesRange.end}
                
                🌡️ Температурные показатели:
                ├─ Минимум: ${response.minTemp.value}°C
                ├─ Максимум: ${response.maxTemp.value}°C
                ├─ Средняя: ${"%.1f".format(response.avgTempValue)}°C
                └─ Перепад: ${response.maxTempDiff.value}°C
                
                ⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯
            """.trimIndent())
        }
    }
}

class WeatherApi {
    private val client = HttpClient.newHttpClient()

    fun request(coords: Coordinates, dates_range: DatesRange): String {

        val url = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=${coords.latitude}&longitude=${coords.longitude}" +
                "&daily=temperature_2m_max,temperature_2m_min" +
                "&timezone=auto" +
                "&start_date=${dates_range.start}" +
                "&end_date=${dates_range.end}"
        try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build()

            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            return response.body()
        } catch (e: Exception) {
            println("Ошибка: ${e.message}")
            return "---"
        }
    }

    fun parsingJSON(json: String, coords: Coordinates, dates_range: DatesRange): WeatherResponse {
        fun extractArray1(key: String): List<String> {
            val regex = "\"$key\":\\s*\\[(.*?)\\]".toRegex()
            val match = regex.find(json)?.groupValues?.get(1) ?: return emptyList()
            return match.split(",").map { it.trim().removeSurrounding("\"") }
        }

        val dates = extractArray1("time")
        val maxTemps = extractArray1("temperature_2m_max").map { it.toDoubleOrNull() }
        val minTemps = extractArray1("temperature_2m_min").map { it.toDoubleOrNull() }

        val minTempWithIndex = minTemps.withIndex()
            .filter { it.value != null }
            .minByOrNull { it.value!! }!!
        val minTemp = minTempWithIndex.value!!
        val minTempDate = dates[minTempWithIndex.index]

        val maxTempWithIndex = maxTemps.withIndex()
            .filter { it.value != null }
            .maxByOrNull { it.value!! }!!
        val maxTemp = maxTempWithIndex.value!!
        val maxTempDate = dates[maxTempWithIndex.index]


        val validTemps = maxTemps.zip(minTemps)
            .filter { it.first != null && it.second != null }
            .map { (it.first!! + it.second!!) / 2 }
        val avgTemp = validTemps.average()


        val tempDiffsWithIndices = maxTemps.zip(minTemps)
            .mapIndexed { index, (max, min) ->
                if (max != null && min != null) index to (max - min) else null
            }
            .filterNotNull()
        val maxDiffWithIndex = tempDiffsWithIndices.maxByOrNull { it.second }!!
        val maxTempDiff = maxDiffWithIndex.second
        val maxDiffDate = dates[maxDiffWithIndex.first]

//        println("Минимальная температура: $minTemp°C, дата: $minTempDate")
//        println("Максимальная температура: $maxTemp°C, дата: $maxTempDate")
//        println("Средняя температура: ${"%.1f".format(avgTemp)}°C")
//        println("Максимальная разница температур: $maxTempDiff°C, дата: $maxDiffDate")

        return WeatherResponse(coords, dates_range, MinTemp(minTemp, LocalDate.parse(minTempDate)), MaxTemp(maxTemp, LocalDate.parse(maxTempDate)), MaxTempDiff(maxTempDiff, LocalDate.parse(maxDiffDate)), avgTemp)
    }
}

class WeatherApp {
    private var isRunning = true
    private val weatherApi = WeatherApi()
    private val cache = WeatherCache()

    public fun process() {
        println("=== Weather Analyzer ===")
        println("Эта программа анализирует данные о температуре для заданных координат и диапазона дат")
        while (isRunning) {
            val coords = ConsoleInput.getCoords()
            val dates_range = ConsoleInput.getDatesRange()
            var response = cache.find(coords, dates_range)
            if (response != null) {
                ConsoleOutput.printResponse(response)
            } else {
                val responseJson = weatherApi.request(coords, dates_range)
                if (responseJson != "---") {
                    response = weatherApi.parsingJSON(responseJson, coords, dates_range)
                    ConsoleOutput.printResponse(response)
                }
            }
            print("\nВы хотите продолжить пользоваться приложением? (yes/no): ")
            val answer = readLine()?.trim()?.lowercase()
            if (answer != "yes" && answer != "y") {
                isRunning = false
            }
            println()
        }
        println("Goodbye!")
    }
}
