package com.dev.demoapplication.presentation

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dev.demoapplication.ui.theme.DemoApplicationTheme
import com.example.link.screens.HomeActivity
import com.example.link.screens.login.LoginActivity

class MainActivity : FragmentActivity() {
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

//                            LoginScreen(
//                                navigateToHome = { navController.navigate("home_screen") }
//                            )
                            val intent = Intent(context, LoginActivity::class.java)
                            context.startActivity(intent)
                        }

                        // Add home screen route
                        composable("home_screen") {
                            HomeScreen()
                        }
                    }
                }
            }
        }
    }
}



