class ConsoleInput(private val coins: List<String>, private val methods: List<String>) {

    fun readUserSettings(): UserSettings {
        val instrument = chooseCoin()
        val windowSeconds = chooseWindow()
        val method = chooseMethod()
        return UserSettings(instrument, windowSeconds, method)
    }

    private fun chooseCoin(): String {
        while (true) {
            println("Выберите монету:")
            coins.forEachIndexed { index, coin -> println("${index + 1}) $coin") }
            val input = readLine()
            val choice = input?.toIntOrNull()
            if (choice != null && choice in 1..coins.size) {
                return coins[choice - 1]
            } else {
                println("Ошибка: введите число от 1 до ${coins.size}. Попробуйте ещё раз.")
            }
        }
    }

    private fun chooseWindow(): Int {
        while (true) {
            println("Введите длительность окна (в секундах), число больше 0:")
            val input = readLine()
            val window = input?.toIntOrNull()
            if (window != null && window > 0) {
                return window
            } else {
                println("Ошибка: введите корректное положительное число.")
            }
        }
    }

    private fun chooseMethod(): String {
        while (true) {
            println("Выберите метод анализа:")
            methods.forEachIndexed { index, method -> println("${index + 1}) $method") }
            val input = readLine()
            val choice = input?.toIntOrNull()
            if (choice != null && choice in 1..methods.size) {
                return methods[choice - 1]
            } else {
                println("Ошибка: введите число от 1 до ${methods.size}. Попробуйте ещё раз.")
            }
        }
    }

    fun needRepeatQuery(): Boolean {
        var input: String? = null
        while (input !in listOf("yes", "y", "no", "n")) {
            println("Вы хотите продолжить? (yes/no)")
            input = readlnOrNull()?.lowercase()
        }
        return (input == "yes" || input == "y")
    }
}
