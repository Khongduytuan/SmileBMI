package com.eagletech.smilebmi

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.eagletech.smilebmi.data.MyData
import com.eagletech.smilebmi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var myData: MyData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myData = MyData.getInstance(this)

        binding.numberPickerHeight.minValue = 0
        binding.numberPickerHeight.maxValue = 250
        binding.numberPickerWeight.minValue = 0
        binding.numberPickerWeight.maxValue = 250

        binding.buttonCalculate.setOnClickListener {
            val height = binding.numberPickerHeight.value.toFloat()
            val weight = binding.numberPickerWeight.value.toFloat()
            if (height == 0.toFloat()) {
                binding.textBMI.text = "Invalid height or weight value"
            } else {
                if (myData.isPremiumSaves == true){
                    val bmi = calculateBMI(height, weight)
                    val bmiStatus = getBMIStatus(bmi)
                    val bmiSuggest = getBMISuggest(bmi)
                    binding.textViewSuggest.text = "$bmiSuggest"
                    binding.textBMI.text = "$bmiStatus"
                }else if(myData.getSaves() > 0){
                    val bmi = calculateBMI(height, weight)
                    val bmiStatus = getBMIStatus(bmi)
                    val bmiSuggest = getBMISuggest(bmi)
                    binding.textViewSuggest.text = "$bmiSuggest"
                    binding.textBMI.text = "$bmiStatus"
                    myData.removeSaves()
                } else{
                    Toast.makeText(this, "Please buy it to use", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, PayActivity::class.java)
                    startActivity(intent)
                }
                
            }
        }
        binding.topBar.buy.setOnClickListener {
            val intent = Intent(this, PayActivity::class.java)
            startActivity(intent)
        }
        binding.topBar.info.setOnClickListener {
            showInfoBuy()
        }
    }
    private fun calculateBMI(height: Float, weight: Float): Float {
        return weight / (height * height)
    }

    private fun getBMIStatus(bmi: Float): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi in 18.5..24.9 -> "Normal"
            bmi in 25.0..29.9 -> "Overweight"
            else -> "Fat"
        }
    }
    private fun getBMISuggest(bmi: Float): String {
        return when {
            bmi < 18.5 -> "Don't skip meals and add healthy snacks to your diet."
            bmi in 18.5..24.9 -> "Maintain a balanced diet and regular exercise."
            bmi in 25.0..29.9 -> "Reduce portion sizes and increase physical activity."
            else -> "Start a serious weight loss plan with a balanced diet and exercise"
        }
    }
    private fun showInfoBuy() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Your usage BMI in my app")
            .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
            .create()
        if (myData.isPremiumSaves == true) {
            dialog.setMessage("Registered to use successfully")
        } else {
            dialog.setMessage("You have ${myData.getSaves()} uses")
        }
        dialog.show()
    }
}