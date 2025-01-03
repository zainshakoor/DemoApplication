package com.dev.demoapplication.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dev.demoapplication.ui.theme.DemoApplicationTheme
import com.example.link.screens.login.LoginActivity

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DemoApplicationTheme {
                // Create a NavController for navigation
                val navController = rememberNavController()

                // Scaffold structure
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Navigation host to manage composables
                    NavHost(
                        navController = navController,
                        startDestination = "login_screen",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Add login screen route
                        composable("login_screen") {
                            val context = LocalContext.current
                            val intent = Intent(context, LoginActivity::class.java)
                            context.startActivity(intent)
                        }

                    }
                }
            }
        }
    }
}



