package com.example.link.screens.amount

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.link.screens.review.ReviewActivity
import com.example.xmlmodule.databinding.ActivityAmountBinding

class AmountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAmountBinding
    private val amountViewModel: AmountViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAmountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences =
            getSharedPreferences(AmountViewModel.PREFS_NAME, Context.MODE_PRIVATE)

        // Add SharedPreferences listener to detect when challenge key is updated
        sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == AmountViewModel.KEY_CHALLENGE) {
                val updatedChallenge =
                    sharedPreferences.getString(AmountViewModel.KEY_CHALLENGE, null)
                Log.d("AmountActivity", "Challenge key updated: $updatedChallenge")
                updatedChallenge?.let {
                    amountViewModel.receiveChallengeKey(it)
                }
            }
        }

        binding.apply {
            buttonContinue.setOnClickListener {
                val amount = EditTextAmount.text.toString()
                if (amount.isNotEmpty()) {
                    // Start loading and observe the loading state
                    amountViewModel.isLoading.observe(this@AmountActivity, { isLoading ->
                        if (isLoading) {
                            // Show the progress bar when loading
                            progressBar.visibility = View.VISIBLE
                        } else {
                            // Hide the progress bar when done
                            progressBar.visibility = View.GONE

                            // Show the toast message when verification is successful
                            Toast.makeText(
                                this@AmountActivity,
                                "Verification Successful",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Proceed with transition to ReviewActivity once loading is done
                            val intent = Intent(this@AmountActivity, ReviewActivity::class.java)
                            intent.putExtra("amount", amount)
                            startActivity(intent)
                        }
                    })

                    // Start the amount flow process
                    amountViewModel.processAmountFlow(this@AmountActivity)
                } else {
                    Toast.makeText(
                        this@AmountActivity,
                        "Please enter an amount",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the listener when activity is destroyed
        val sharedPreferences =
            getSharedPreferences(AmountViewModel.PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener { _, _ -> }
    }
}
