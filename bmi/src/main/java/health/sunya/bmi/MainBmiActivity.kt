package health.sunya.bmi

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.gson.Gson
import health.sunya.bmi.databinding.ActivityBmiLayoutBinding
import org.json.JSONObject
import sunya.health.utils.activity.ActionBarActivity
import sunya.health.utils.constants.IntentParams.PATIENT_PARAM
import sunya.health.utils.drawable.TextDrawable
import sunya.health.utils.helper.DialogHelper
import java.time.LocalDate
import java.time.Period

const val BMI_VALUE_INTENT = "BmiValue"

class MainBmiActivity : ActionBarActivity() {
    private lateinit var patient: JSONObject
    private lateinit var binding: ActivityBmiLayoutBinding
    private lateinit var patientGender: String
    private var isFemale: Boolean = false
    private lateinit var patientName: String
    private lateinit var patientEmail: String
    private var weightType: String = "KG"
    private var heightType: String = "M"
    private var age = 0
    private var patientSync: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBmiLayoutBinding.inflate(layoutInflater)

        getPatientDetails()

        checkIfFemale()

        setInitialValues()

        setDropDown()

        binding.actBmiCalcBtn.setOnClickListener {
            calculate()
        }

        setView(
            binding.root,
            getString(R.string.bmi_test),
            getString(R.string.bmi_subtitle),
            sunya.health.utils.R.drawable.ic_bmi
        )
    }

    private fun getAge(year: Int, month: Int, dayOfMonth: Int): Int {
        return Period.between(
            LocalDate.of(year, month + 1, dayOfMonth),
            LocalDate.now()
        ).years
    }

    private fun getPatientDetails() {
        patient = JSONObject(intent.getStringExtra(PATIENT_PARAM)!!)
        patientName = patient.getString("name") ?: ""
        patientEmail = patient.getString("email") ?: ""
        patientGender = patient.getString("gender") ?: ""
        patientSync = patient.getBoolean("synced")
        age = patient.getInt("age")

    }

    private fun setInitialValues() {
        binding.apply {
            personDetailLyt.apply {
                personDetailLytNameTV.text = patientName
                personDetailLytEmailTV.text = patientEmail
                personDetailLytPhotoIV.setImageDrawable(
                    TextDrawable.builder().buildRound(patientName[0])
                )
                personDetailLytSeeDetailsTV.visibility = GONE
                if (patientSync) {
                    personDetailLytActiveOrInActiveView.setBackgroundResource(sunya.health.utils.R.drawable.ic_active_status)
                } else {
                    personDetailLytActiveOrInActiveView.setBackgroundResource(sunya.health.utils.R.drawable.ic_not_active)
                }
                personDetailLytPhotoIV.setImageDrawable(
                    TextDrawable.builder().buildRound(patientName.trim()[0])
                )
                binding.actBmiAgeValueTV.text = age.toString()
                if (isFemale) {
                    bmiFemaleGenderCard.setImageResource(R.drawable.card_ticked)
                } else {
                    bmiMaleGenderCard.setImageResource(R.drawable.card_ticked)
                }
            }
        }
    }

    private fun checkIfFemale() {
        when {
            patientGender.lowercase() == "female" -> {
                isFemale = true
            }
            patientGender.lowercase() == "male" -> {
                isFemale = false
            }
            else -> {
                Toast.makeText(
                    this,
                    getString(R.string.bmi_not_calculated_for_others),
                    Toast.LENGTH_SHORT
                ).show()
                binding.actBmiCalcBtn.isEnabled = false
            }
        }
    }

    private fun setDropDown() {
        val selectKgLb = resources.getStringArray(sunya.health.utils.R.array.kg_lb)
        val weightArrayAdapter =
            ArrayAdapter(this, sunya.health.utils.R.layout.drop_down_text_view, selectKgLb)
        binding.actBmiWeightTypeAutoTV.setAdapter(weightArrayAdapter)
        val selectInM = resources.getStringArray(sunya.health.utils.R.array.in_m)
        val heightArrayAdapter =
            ArrayAdapter(this, sunya.health.utils.R.layout.drop_down_text_view, selectInM)
        binding.actBmiHeightTypeInputAutoTV.setAdapter(heightArrayAdapter)
    }


    private fun calculate() {
        if (checkInputFields()) {
            val age: Double = binding.actBmiAgeValueTV.text.toString().toDouble() * 12.0
            var height: Double = binding.actBmiHeightEditV.text.toString().toDouble()
            var weight: Double = binding.actBmiWeightInputField.text.toString().toDouble()
            val isfemale: Boolean = isFemale

            if (binding.actBmiWeightTypeAutoTV.text.toString().isNotEmpty()) {
                weightType = binding.actBmiWeightTypeAutoTV.text.toString()
            }
            if (binding.actBmiHeightTypeInputAutoTV.text.toString().isNotEmpty()) {
                heightType = binding.actBmiHeightTypeInputAutoTV.text.toString()
            }

            //max lb =400
            //  m = 2.5
            // kg = 180
            //min kg =4.53
            //min height 0.25

            if (weightType == "LB") {
                weight *= 0.453592
            }
            if (heightType == "IN") {
                height *= 0.0254
            }


            val bmivalue: Double = weight / (height * height)
            if (age > 240.5 || age <= 24.0) {
                val category = BmiCalculator.getSimpleBmiCategory(bmivalue)
                val result = BmiCalculator.setBmiValues(age, weight, height, bmivalue, "", category)
                setResults(result)
            } else {
                if (weight > 180.0 || height > 2.5 || height < 0.254 || weight < 4.53) {
                    if (weight > 180.0) {
                        Toast.makeText(
                            this,
                            getString(R.string.weight_cant_400),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (height > 2.5) {
                        Toast.makeText(
                            this,
                            getString(R.string.height_cant_100),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                    if (height < 0.254) {
                        Toast.makeText(
                            this,
                            getString(R.string.height_cant_10),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    if (weight < 4.53) {
                        Toast.makeText(
                            this,
                            getString(R.string.weight_cant_10),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    try {
                        val result = BmiCalculator.readExcelData(
                            age,
                            weight,
                            height,
                            isfemale,
                            bmivalue,
                            applicationContext
                        )
                        setResults(result)
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                }
            }

        }
    }


    private fun checkInputFields(): Boolean {
        try {
            if (binding.actBmiHeightEditV.text.isNullOrEmpty()
                && binding.actBmiWeightInputField.text.isNullOrEmpty()
            ) {
                Toast.makeText(this, getString(R.string.plz_enter_all_fields), Toast.LENGTH_SHORT)
                    .show()
            } else if (binding.actBmiHeightEditV.text.isNullOrEmpty()) {
                binding.actBmiWeightLyt.error = getString(R.string.enter_weight)
            } else if (binding.actBmiWeightInputField.text.isNullOrEmpty()) {
                binding.actBmiHeightInputLyt.error = getString(R.string.enter_weight)
            } else if (
                !(
                        binding.actBmiHeightEditV.text.isNullOrEmpty()
                                && binding.actBmiWeightInputField.text.isNullOrEmpty()
                        )
            ) {
                return if (binding.actBmiHeightEditV.text.toString().toDouble() < 0
                    || binding.actBmiWeightInputField.text.toString().toDouble() < 0
                ) {
                    Toast.makeText(
                        this,
                        getString(R.string.val_cant_be_less_zero),
                        Toast.LENGTH_SHORT
                    ).show()
                    false
                } else {
                    binding.actBmiWeightLyt.isErrorEnabled = false
                    binding.actBmiHeightInputLyt.isErrorEnabled = false
                    true
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.something_wrong_with_input),
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
        } catch (e: java.lang.NumberFormatException) {
            Toast.makeText(this, getString(R.string.invalid_data), Toast.LENGTH_SHORT).show()
        }
        return false
    }


    private fun setResults(bmiResult: BmiTestResults) {
        val resultIntent = Intent()
        resultIntent.putExtra(BMI_VALUE_INTENT, Gson().toJson(bmiResult))
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    override fun onBackPressed() {
        val yesBtnClick = { _: DialogInterface, _: Int ->
            super.onBackPressed()
        }
        val noBtnClick = { _: DialogInterface, _: Int ->
            DialogHelper.dismissBackConfirmationDialog()
        }
        DialogHelper.startBackConfirmationDialog(
            context = this,
            positiveButtonClick = yesBtnClick,
            negativeButtonClick = noBtnClick
        )
    }


}