package health.sunya.bmi

data class BmiTestResults(
    val bmiValue: Double,
    val bmiPercentile: String = "",
    val bmiCategory: String,
    val age: Int,
    val height: Double,
    val weight: Double,
    )
