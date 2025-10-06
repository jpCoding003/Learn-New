package com.tops.learnnew

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tops.learnnew.Model.RateResponse
import com.tops.learnnew.databinding.ActivityCurrencyConverterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CurrencyConverterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCurrencyConverterBinding

    private val currencies = listOf("USD", "INR", "EUR", "GBP", "JPY", "AUD", "CAD")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCurrencyConverterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Spinner setup
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerFrom.adapter = adapter
        binding.spinnerTo.adapter = adapter

        // Button click
        binding.btnConvert.setOnClickListener {
            convertCurrency()
        }
    }

    private fun convertCurrency() {
        val amountText = binding.etAmount.text.toString()
        if (amountText.isEmpty()) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDouble()
        val fromCurrency = binding.spinnerFrom.selectedItem.toString()
        val toCurrency = binding.spinnerTo.selectedItem.toString()

        // API call
        val call = RetrofitInstance.api.getExchangeRate(fromCurrency, toCurrency)
        call.enqueue(object : Callback<RateResponse> {
            override fun onResponse(call: Call<RateResponse>, response: Response<RateResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val rate = response.body()!!.rates[toCurrency]
                    if (rate != null) {
                        val result = amount * rate
                        binding.tvResult.text = "$amount $fromCurrency = %.2f $toCurrency".format(result)
                    } else {
                        binding.tvResult.text = "Conversion rate not found"
                    }
                } else {
                    binding.tvResult.text = "Failed to get exchange rate"
                }
            }

            override fun onFailure(call: Call<RateResponse>, t: Throwable) {
                binding.tvResult.text = "Error: ${t.localizedMessage}"
            }
        })
    }
}
