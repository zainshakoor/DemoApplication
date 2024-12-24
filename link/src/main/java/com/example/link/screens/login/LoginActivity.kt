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
import com.example.link.screens.HomeActivity
import com.example.link.viewmodel.LoginViewModel
import com.example.xmlmodule.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    companion object {
        const val PREFS_NAME = "MyAppPreferences"
        const val KEY_FCM_TOKEN = "firebase_token"
        const val KEY_CHALLENGE = "challenge"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI components
        initializeUI()

        // Start FCM token retrieval
        loginViewModel.retrieveAndStoreFCMToken(this)

        // Observe the loading state to show/hide the progress bar
        observeLoadingState()
    }

    /**
     * Initializes the UI components and sets up the button listener.
     */
    private fun initializeUI() {
        // Set up button to trigger the login flow
        binding.buttonSignin.setOnClickListener {
            Log.d("LoginActivity", "Login button clicked.")
            performLoginFlow()
        }

        loginViewModel.message.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Calls performLoginFlow function from the ViewModel.
     */
    @SuppressLint("NewApi")
    private fun performLoginFlow() {
        // Get the FCM token from SharedPreferences
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val fcmToken = sharedPreferences.getString(KEY_FCM_TOKEN, null)

        if (fcmToken.isNullOrEmpty()) {
            Log.e("LoginActivity", "FCM Token is not available. Cannot proceed with registration.")
            return
        }

        // Perform login using the ViewModel
        lifecycleScope.launch {
            // Start the login flow
            loginViewModel.performLoginFlow(this@LoginActivity)

            // Wait for the loading state to become false
            loginViewModel.isLoading.collect { loading ->
                if (!loading) {
                    // After the login flow is complete, show a success toast
                    showLoginSuccessToast()

                    // Navigate to the HomeActivity after successful login
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                }
            }
        }
    }


    /**
     * Shows a toast message after a successful login.
     */
    private fun showLoginSuccessToast() {
        Toast.makeText(this, "Registration and Authentication Successful!", Toast.LENGTH_SHORT)
            .show()
    }

    /**
     * Observes the loading state from the ViewModel to show/hide the progress bar.
     */
    private fun observeLoadingState() {
        lifecycleScope.launch {
            loginViewModel.isLoading.collect { loading ->
                // Show or hide the progress bar based on the loading state
                binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }
    }

    /**
     * Retrieves the device ID.
     */
    private fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }

    override fun onStart() {
        super.onStart()
        // Register the listener for SharedPreferences
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        // Unregister the listener for SharedPreferences
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * Callback method to be invoked when a shared preference is changed.
     */
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
