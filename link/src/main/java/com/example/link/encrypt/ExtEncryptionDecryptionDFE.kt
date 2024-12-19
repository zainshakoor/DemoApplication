package com.example.link.encrypt


import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import com.google.android.gms.common.util.Base64Utils
import java.math.BigInteger
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.spec.RSAKeyGenParameterSpec
import javax.crypto.Cipher

object ExtEncryptionDecryptionDFE {
    const val TAG = "ExtEncryptionDecryptionDFE"
    private const val KEY_ALIAS = "MyRSAKeyAlias"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"

    private lateinit var keyPair: KeyPair

    init {
        if (!isKeyPairExists()) {
            generateRSAKeyPair()
        }
        keyPair = getKeyPair()
        Log.d(TAG, "RSA KeyPair initialized")

        // Log the public key in Base64 format for use in Node.js
        val publicKeyBase64 = Base64Utils.encode(keyPair.public.encoded).trim().replace("\n", "")
        Log.d(TAG, "Public Key: $publicKeyBase64")
    }

    /**
     * Checks if the key pair already exists in the Keystore.
     */
    private fun isKeyPairExists(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }
            keyStore.containsAlias(KEY_ALIAS)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking key existence", e)
            false
        }
    }

    /**
     * Generates an RSA key pair and stores it in the Keystore.
     */
    private fun generateRSAKeyPair() {
        try {
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA,
                ANDROID_KEYSTORE
            )
            val parameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_DECRYPT // Only DECRYPT is needed for decryption
            )
                .setAlgorithmParameterSpec(RSAKeyGenParameterSpec(2048, BigInteger.valueOf(65537)))
                .setBlockModes(KeyProperties.BLOCK_MODE_ECB)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setUserAuthenticationRequired(false)
                .build()
            keyPairGenerator.initialize(parameterSpec)
            keyPairGenerator.generateKeyPair()
            Log.d(TAG, "RSA KeyPair generated and stored in Keystore")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating RSA KeyPair", e)
        }
    }

    /**
     * Retrieves the RSA key pair from the Keystore.
     */
    private fun getKeyPair(): KeyPair {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
                load(null)
            }
            val privateKey = keyStore.getKey(KEY_ALIAS, null) as PrivateKey
            val publicKey = keyStore.getCertificate(KEY_ALIAS).publicKey
            KeyPair(publicKey, privateKey)
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving RSA KeyPair from Keystore", e)
            throw RuntimeException("Failed to get key pair", e)
        }
    }

    /**
     * Decrypts a Base64-encoded string that was encrypted using the corresponding public key.
     *
     * @param encryptedData Base64-encoded encrypted string.
     * @return The decrypted string, or an empty string if decryption fails.
     */
    fun decrypt(encryptedData: String): String {
        return try {
            if (encryptedData.isEmpty()) return ""

            // Decode the Base64-encoded string
            val encryptedBytes = Base64Utils.decode(encryptedData.trim())
            Log.d(TAG, "Encrypted Bytes Length: ${encryptedBytes.size}")

            // Initialize Cipher for decryption
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            println(keyPair.private)
            cipher.init(Cipher.DECRYPT_MODE, keyPair.private)
            Log.d(TAG, "Cipher initialized for decryption")

            // Perform decryption
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            Log.d(TAG, "Decryption successful, Decrypted Bytes Length: ${decryptedBytes.size}")

            String(decryptedBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption error: ${e.message}", e)
            ""
        }
    }
}


fun String.decryptDfe(): String {
    return ExtEncryptionDecryptionDFE.decrypt(this)
}

