package com.example.link.screens.dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.link.screens.amount.AmountActivity
import com.example.xmlmodule.databinding.ActivityDashBoardBinding

class DashBoardActivity : AppCompatActivity() {


    private lateinit var binding: ActivityDashBoardBinding

    private val dashBoardViewModel: DashBoardViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDashBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.apply {
            buttonSendMoney.setOnClickListener {
                val intent = Intent(this@DashBoardActivity, AmountActivity::class.java)
                startActivity(intent)

            }

        }

        dashBoardViewModel.greetings.observe(
            this
        ) { message ->
            binding.textViewGreetings.text = message.toString()

        }
        dashBoardViewModel.UpdateGreetingMessage()
    }
}