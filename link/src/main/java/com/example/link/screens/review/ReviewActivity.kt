package com.example.link.screens.review

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.link.screens.amount.AmountActivity
import com.example.link.screens.dashboard.DashBoardActivity
import com.example.xmlmodule.R
import com.example.xmlmodule.databinding.ActivityReviewBinding

class ReviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val amount = intent.getStringExtra("amount")
        if (amount != null) {
            binding.textViewBalanceAmount.text =" $amount"
        } else {
            Toast.makeText(this, "No amount received", Toast.LENGTH_SHORT).show()
        }



        binding.buttonPayNow.setOnClickListener {

            val intent = Intent(this,DashBoardActivity::class.java)
            startActivity(intent)
            Toast.makeText(this, "Payment Success ", Toast.LENGTH_LONG).show()

        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}