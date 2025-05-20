class ConsoleOutput {

    fun printStartInfo(settings: UserSettings) {
        println("▶️ Запуск анализа: ${settings.instrument} | Окно: ${settings.windowSeconds} сек | Метод: ${settings.method}")
    }

    fun printProgress(current: Int, total: Int, price: Double, analysisResult: Double, method: String) {
        val output = when (method) {
            "average" -> "${"%.2f".format(analysisResult)} ${progressBar(current, total)}"
            else -> "%.2f".format(analysisResult)
        }
        println("[$current/$total] Цена: $price | Анализ: $output")
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
