package com.example.link.screens

//import com.example.link.encrypt.ExtEncryptionDecryption.decrypt
//import com.example.link.EncryptDecryptRSA
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.link.viewmodel.HomeViewModel
import com.example.xmlmodule.databinding.ActivityHomeBinding
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var binding: ActivityHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
//    private val rsa= EncryptDecryptRSA()

    companion object {
        const val PREFS_NAME = "MyAppPreferences"
        const val KEY_FCM_TOKEN = "firebase_token"
        const val KEY_CHALLENGE = "challenge"
    }

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI components
        initializeUI()
        binding.progress.visibility = View.GONE

        // Start FCM token retrieval
        homeViewModel.retrieveAndStoreFCMToken(this)
    }

    /**
     * Initializes the UI components and sets up the button listener.
     */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun initializeUI() {
        //   Single Button to Start the Flow
        binding.buttonRegister.setOnClickListener {
            Log.d("HomeActivity", "button clicked.")
            binding.progress.visibility = View.VISIBLE
            homeViewModel.performRegisterVerifySendFlow(this)

        }




        homeViewModel.message.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
        lifecycleScope.launch {
            homeViewModel.isLoading.collect { loading ->
                binding.progress.visibility = if (loading) View.VISIBLE else View.GONE
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
        // Register the listener
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        super.onStop()
        // Unregister the listener
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * Callback method to be invoked when a shared preference is changed.
     */
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == KEY_CHALLENGE) {
            val updatedChallenge = sharedPreferences?.getString(KEY_CHALLENGE, null)
            Log.d("HomeActivity", "Challenge key updated: $updatedChallenge")
            updatedChallenge?.let {
                homeViewModel.receiveChallengeKey(it)
            }
        }
    }
}
