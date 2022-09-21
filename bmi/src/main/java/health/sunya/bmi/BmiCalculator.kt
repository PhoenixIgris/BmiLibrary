package health.sunya.bmi

import android.content.Context
import org.apache.poi.ss.usermodel.WorkbookFactory

object BmiCalculator {
    fun readExcelData(
        age: Double,
        weight: Double,
        height: String,
        isFemale: Boolean?,
        bmiValue: Double?,
        context: Context
    ): BmiTestResults {
        val inputStream = context.assets.open("data.xls")
        val xlWb = WorkbookFactory.create(inputStream)
        val page = if (isFemale!!) {
            0
        } else {
            1
        }
        var xlWs = xlWb.getSheetAt(page)
        var xlRows = xlWs.rowIterator()
        var ans: Double?
        var L: Double? = null
        var M: Double? = null
        var S: Double? = null
        for (i in xlRows) {
            ans = i.getCell(1).toString().toDouble()
            if ((ans >= age)) {
                L = i.getCell(2).toString().toDouble()
                M = i.getCell(3).toString().toDouble()
                S = i.getCell(4).toString().toDouble()
                break
            }
        }
        val zScore = (((Math.pow((bmiValue!! / M!!), L!!)) - 1) / (L * S!!))
        if (zScore > 3.0) {
            return setBmiValues(
                age,
                weight,
                height,
                bmiValue,
                "100.0",
                getSimpleBmiCategory(bmiValue)
            )
        } else if (zScore < -3.0) {
            return setBmiValues(
                age,
                weight,
                height,
                bmiValue,
                "0.0",
                getSimpleBmiCategory(bmiValue)
            )
        } else {
            val first = zScore.toString().substring(0, zScore.toString().indexOf(".") + 2)
            val second = zScore.toString()
                .substring(
                    zScore.toString().indexOf(".") + 2,
                    zScore.toString().indexOf(".") + 3
                )
                .toInt()
            xlWs = xlWb.getSheetAt(2)
            xlRows = xlWs.rowIterator()
            var answer: String?
            var percentile: String? = null
            for (i in xlRows) {
                answer = i.getCell(0).toString()
                if (answer == first) {
                    percentile = i.getCell(second + 1).toString()
                    break
                }
            }
            if (percentile!!.contains("%")) {
                percentile = percentile.replace("%", "")
            }
            val test = percentile.toDouble()
            val category: String = when {
                test < 5.0 -> "Under Weight"
                test >= 5.0 && test < 85.0 -> "Healthy Weight"
                test >= 85.0 && test < 95.0 -> "Over Weight"
                else -> "Obese"
            }
            return setBmiValues(age, weight, height, bmiValue, percentile, category)
        }
    }

    fun getSimpleBmiCategory(bmivalue: Double): String {
        val category: String = when {
            bmivalue < 18.5 -> "Under Weight"
            bmivalue >= 18.5 && bmivalue < 25 -> "Healthy Weight"
            bmivalue >= 25 && bmivalue < 30 -> "Over Weight"
            bmivalue >= 30 && bmivalue < 35 -> "Obese (Class I)"
            bmivalue >= 35 && bmivalue < 40 -> "Obese (Class II)"
            else -> "Obese (Class III)"
        }
        return category
    }

    fun setBmiValues(
        age: Double,
        weight: Double,
        height: String,
        bmiValue: Double,
        percentile: String,
        category: String,
    ): BmiTestResults {
        return BmiTestResults(
            bmiValue = String.format("%.2f", bmiValue).toDouble(),
            bmiPercentile = percentile,
            bmiCategory = category,
            age = (age / 12.0).toInt(),
            weight = String.format("%.2f", weight).toDouble(),
            height = height,
        )
    }
}