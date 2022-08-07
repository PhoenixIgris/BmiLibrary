package health.sunya.bmi

data class BmiTestResults(
    val bmiValue: Double,
    val bmiPercentile: String = "",
    val bmiCategory: String,
    val age: Int,
    val height: String,
    val weight: Double,
    )
