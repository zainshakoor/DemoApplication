package com.example.link.screens

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.link.encrypt.ExtEncryptionDecryption
import com.example.link.encrypt.ExtEncryptionDecryption.decrypt
import com.example.link.viewmodel.HomeViewModel
import com.example.xmlmodule.databinding.ActivityHomeBinding
import kotlinx.coroutines.launch
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import android.util.Base64
import android.util.Log
import com.example.link.EncryptDecryptRSA
import com.example.link.encrypt.ExtEncryptionDecryptionDFE
import com.example.link.encrypt.decryptDfe
import kotlin.math.log

class HomeActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var binding: ActivityHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private val rsa= EncryptDecryptRSA()

    companion object {
        const val PREFS_NAME = "MyAppPreferences"
        const val KEY_FCM_TOKEN = "firebase_token"
        const val KEY_CHALLENGE = "challenge"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UI components
        initializeUI()
        binding.progress.visibility=View.GONE

        // Start FCM token retrieval
        homeViewModel.retrieveAndStoreFCMToken(this)
    }

    /**
     * Initializes the UI components and sets up the button listener.
     */
    private fun initializeUI() {
      //   Single Button to Start the Flow
//        binding.buttonRegister.setOnClickListener {
//            Log.d("HomeActivity", "button clicked.")
//            binding.progress.visibility= View.VISIBLE
//            homeViewModel.performRegisterVerifySendFlow(this)
//
//        }

//            val decryptmesg1="bgtHNtpm48hCnWkK21Gq0A9QmRTekGs/ymhmWm/h42N4omd8QPQMZCCA6erj21e4SNvdhMsrbnlhRyG7sZLItJu/obaEbs/hkhKjCdmn0DE23G6M1fSbHsyaM2NQU7JQi+cVigsnLXThM7011g+UvgTDsTnybMHJqsumrwGLHzmq5yg1wwtJIosZZgj3zCKkuVf1P/GhOBwv+pmvoKFYfk48U/okqxjpRMKfV9Wd6DsCqjW1I6JQWS/8fAf2NYCqv1fPiYkL/x2CUO2TvBhsNioHuFUwolt/4ozJPfh03FeGMOU5Nd4U5Rd8Qc+1lfkcOtiA4Ke/Al1sUUmWgdAfhw=="
//           println(decryptmesg1.decryptDfe())
//            en.encryptData("Hello this is a tet")

        binding.buttonGenrateKey.setOnClickListener {
            rsa.generateKeyPair()
            val publicKey=rsa.getPublicKey()
            val publicKeyBase64 = Base64.encodeToString(publicKey.encoded, Base64.DEFAULT)
            println("Public Key (Base64): $publicKeyBase64")
        }

        binding.buttonRegister.setOnClickListener {
            val encryptedBase64= "lcwFyp20ByZOl8f6q8+8F/NNjdahW/b1ZTC5/V7j4aHa+5B3uLpSgs3K0W6AiyM86aDjBv2GSrZL82suLP9aAz3Shsjc78RjAX4LdRnmQLBHaRk79i3Nicz+93GkMYguaR3wF1kqmxMV0ZIpwr5XFX1Z14LwDbEPqc/5RjhGcd7dbtsDER8qg6NIsz2wlZeb2/HU0lGz5WZv6aDSWnqku50mwuPu6JPJKf2RWs+TgRzoooXgWTioEL6muXVL59O80CkdCHqJgM2bRELcTDebOM+fkC0Qe5mnC48oa/Z4gMq2iLdC+wWGF1g6GCtI6jCuD6NgL7sgYtAvTfY9Panq/g=="
//            val encryptedBase64= "ghmCn12iVVCxfJYFBB78I2yYdtUYSEs70iEVOv0U0VPwSODvxve5TuJxDhHAO/sRS3Pt19+cTuGmbMA+n2vWkrbBdMzM4GN+gvkaCRE1Up5j487bTkDeT2UWuoeZVpM0F4zalQ93y/3NiJVayxdglxnuoRyoV75WrvN+4p6OVVEoZxOXoaZThOWgWSXATazEMRKYucz20tZUe9rovz0Tz0SF4h3iwXG5LxnghovGdMjZQj4ZqbriuP97M6T4OQtoK+SHw4/R0CTj95hQRPPPeE8inDY5s9+5/ghwv2kRI/+AXEw6j8GXQJ9e4kVspDzNAGKoZyGT4JAfQCXzl4S1Dg=="
            println(rsa.decryptDataFromBase64(encryptedBase64))
        }


        homeViewModel.message.observe(this) {
            message ->
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
