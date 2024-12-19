package com.example.a2fa

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.a2fa.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private  lateinit var  binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.buttonRegister.setOnClickListener {

            Toast.makeText(applicationContext,"sksjjjdd",Toast.LENGTH_LONG).show()

        }





    }
}