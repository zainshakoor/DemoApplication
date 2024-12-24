package com.example.link.screens

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.xmlmodule.databinding.ActivityAmountBinding

class AmountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAmountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAmountBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}
