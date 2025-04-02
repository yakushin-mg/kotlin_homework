import java.util.UUID

// Тип для промтов
data class Prompt(val id: String, val text: String)

// Так как можно использовать кастомный генератор, я использую интерфейс, чтобы было легче добавлять новый генератор
interface Generator {
    fun generateRequests(template: String, data: List<Map<String, Any>>): List<Prompt>
}

class MyGenerator: Generator {
    private fun replaceValues(text: String, data: Map<String, Any>): String {
        var newText = text
        for ((key, value) in data) {
            newText = newText.replace("{$key}", value.toString())
        }
        return newText
    }

    public override fun generateRequests(template: String, data: List<Map<String, Any>>): List<Prompt> {
        val requests = ArrayList<Prompt>()
        for (row in data) {
            val text = replaceValues(template, row)
            val id = UUID.randomUUID().toString()
            requests.add(Prompt(id, text))
        }
        return requests
    }
}

// Тип для ответа на запрос
data class Response(val id: String, val textResponse: String)

// Так как можно использовать другие LLM, я использую интерфейс (аналогично как с генератором)
interface ClientLLM {
    fun executeRequest(textPrompt: String): String
    fun executeRequests(prompts: List<Prompt>): List<Response>
}

class FakeClient(): ClientLLM {
    override fun executeRequest(textPrompt: String): String {
        // тут должен быть запрос к LLM, но я сделаю генерацию мусора
        Thread.sleep(5000)
        return "Fake response for " + textPrompt + "\nResponse " + UUID.randomUUID().toString()
    }

    override public fun executeRequests(prompts: List<Prompt>): List<Response> {
        val responses = ArrayList<Response>()
        for (prompt in prompts) {
            responses.add(Response(prompt.id, executeRequest(prompt.text)))
        }
        return responses
    }
}

// Сервис для генерации ответов на запросы, используя нужный генератор и LLM модель
class App(private val generator: Generator, private val modelLLM: ClientLLM) {
    public fun getResponses(template: String, data: List<Map<String, Any>>): List<Response> {
        return modelLLM.executeRequests(generator.generateRequests(template, data))
    }
}

fun main() {
    // Пример работы
    val testTemplate = "User: {name}, Date: {date}, Request: {request}"
    val testData = listOf(
        mapOf(
            "name" to "Alice2005",
            "date" to "01-01-2025",
            "request" to "How to print HelloWorld in python?"
        ),
        mapOf(
            "name" to "BobСool",
            "date" to "01-01-2025",
            "request" to "How to print HelloWorld in c++?"
        ),
        mapOf(
            "name" to "John555",
            "date" to "01-01-2025",
            "request" to "How to print HelloWorld in my head?"
        )
    )

    val result = App(MyGenerator(), FakeClient()).getResponses(testTemplate, testData)
    result.forEach { response ->
        println("ID " + response.id.toString() + "Text: " + response.textResponse)
    }
}
