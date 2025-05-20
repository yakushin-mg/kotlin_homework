class ConsoleOutput {
    fun printStartInfo(settings: UserSettings) {
        println("▶️ Запуск анализа: ${settings.instrument} | Окно: ${settings.windowSeconds} сек | Метод: ${settings.method}")
    }

    fun print(method: String, current: Int, total: Int, currentPrice: Double, analysisResult: Double) {
        val formattedResult = "%.2f".format(analysisResult)
        val bar = progressBar(current, total)

        when (method) {
            "Средняя цена" -> println("[$current/$total] Цена: $currentPrice | Средняя: $formattedResult $bar")
            "Линейная экстраполяция" -> println("[$current/$total] Цена: $currentPrice | Экстраполяция: $formattedResult $bar")
            else -> println("[$current/$total] Цена: $currentPrice | Анализ: $formattedResult $bar")
        }
    }

    fun printError(current: Int, total: Int) {
        println("[$current/$total] ⚠️ Ошибка запроса")
    }

    private fun progressBar(current: Int, total: Int, width: Int = 20): String {
        val filled = (current * width) / total
        return buildString {
            append(" [")
            append("▓".repeat(filled))
            append("░".repeat(width - filled))
            append("]")
        }
    }
}