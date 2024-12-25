package com.example.link.screens.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.link.screens.dashboard.DashBoardActivity
import com.example.link.viewmodel.LoginViewModel
import com.example.xmlmodule.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

class LoginActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    companion object {
        const val PREFS_NAME = "MyAppPreferences"
        const val KEY_FCM_TOKEN = "firebase_token"
        const val KEY_CHALLENGE = "challenge"
        const val USER_NAME = "kcp343"
        const val PASSWORD = "kcp343"
    }

    private var currentToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI components
        initializeUI()

        // Start FCM token retrieval
        loginViewModel.retrieveAndStoreFCMToken(this)

        // Observe the loading state and message flow directly with collect
        observeFlows()
    }

    private fun initializeUI() {
        binding.EditTextTaskUsername.setText(USER_NAME)
        binding.EditTextPassword.setText(PASSWORD)

        binding.buttonSignin.setOnClickListener {
            Log.d("LoginActivity", "Login button clicked.")
            performLoginFlow()
        }
    }

    @SuppressLint("NewApi")
    private fun performLoginFlow() {
        val username = binding.EditTextTaskUsername.text.toString()
        val password = binding.EditTextPassword.text.toString()

        if (username.isEmpty() || password.isEmpty()) {
            showToast("Username and password must not be empty.")
            return
        }

        if (username == USER_NAME && password == PASSWORD) {
            val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val fcmToken = sharedPreferences.getString(KEY_FCM_TOKEN, null)

            if (fcmToken.isNullOrEmpty()) {
                Log.e("LoginActivity", "FCM Token is not available. Cannot proceed with registration.")
                return
            }

            lifecycleScope.launch {
                loginViewModel.performLoginFlow(this@LoginActivity)
            }
        } else {
            showToast("Invalid username or password.")
        }
    }

    private fun showToast(message: String) {
        currentToast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        currentToast?.show()
    }

    private fun navigateToDashboard() {
        val intent = Intent(this@LoginActivity, DashBoardActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun observeFlows() {
        lifecycleScope.launch {
            // Collect loading state flow
            loginViewModel.isLoading.collect { loading ->
                binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            // Collect message flow
            loginViewModel.messageFlow.collect { message ->
                showToast(message)

                when (message) {
                    "Device Registration Successful!" -> {
                        navigateToDashboard()
                    }
                    "Device is already registered. Proceeding..." -> {
                        navigateToDashboard()
                    }
                }
            }
        }
    }

    private fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    override fun onStart() {
        super.onStart()
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == KEY_CHALLENGE) {
            val updatedChallenge = sharedPreferences?.getString(KEY_CHALLENGE, null)
            Log.d("LoginActivity", "Challenge key updated: $updatedChallenge")
            updatedChallenge?.let {
                loginViewModel.receiveChallengeKey(it)
            }
        }
    }
}
