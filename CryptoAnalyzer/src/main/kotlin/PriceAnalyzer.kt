data class PricePoint(
    val timestamp: Long,
    val price: Double
)

class PriceAnalyzer {
    fun linearExtrapolation(data: List<PricePoint>): Double {
        val n = data.size
        if (n < 2) return data.lastOrNull()?.price ?: 0.0

        val start = data.first().timestamp.toDouble()
        val xs = data.map { (it.timestamp - start) / 1000.0 }
        val ys = data.map { it.price }

        val xAvg = xs.average()
        val yAvg = ys.average()

        val numerator = xs.zip(ys).sumOf { (x, y) -> (x - xAvg) * (y - yAvg) }
        val denominator = xs.sumOf { x -> (x - xAvg) * (x - xAvg) }

        if (denominator == 0.0) return ys.last()

        val slope = numerator / denominator
        val intercept = yAvg - slope * xAvg
        val nextX = xs.last() + 1.0
        return slope * nextX + intercept
    }

    fun average(data: List<PricePoint>): Double {
        return data.map { it.price }.average()
    }
}
