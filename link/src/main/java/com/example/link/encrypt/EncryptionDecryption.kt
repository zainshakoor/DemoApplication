package com.example.link.encrypt

import android.util.Log
import com.google.android.gms.common.util.Base64Utils
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

object ExtEncryptionDecryption {
    private val TAG = ExtEncryptionDecryption::class.java.name

    var myPublicKey: String = ""
    var myPrivateKey: String = ""

    init {
        // Generate RSA Key Pair with 2048-bit key size
        val keyPair = generateRSAKeyPair(2048)

        // Convert the generated keys to Base64 encoding (DER format)
        myPublicKey = Base64Utils.encode(keyPair.public.encoded)
        myPrivateKey = Base64Utils.encode(keyPair.private.encoded)

        Log.d("Public Key", myPublicKey)
        Log.d("Private Key", myPrivateKey)
    }

    // Function to generate RSA Key Pair with specified key size
    private fun generateRSAKeyPair(keySize: Int): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(keySize) // Set the key size (2048 bits)
        return keyGen.generateKeyPair()
    }

    private fun getPublicKeyFromDer(base64PublicKey: String): PublicKey? {
        return try {
            val publicKeyDer = Base64Utils.decode(base64PublicKey)
            val keySpec = X509EncodedKeySpec(publicKeyDer)
            val keyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePublic(keySpec)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getPrivateKeyFromDer(base64PrivateKey: String): PrivateKey? {
        return try {
            val privateKeyDer = Base64Utils.decode(base64PrivateKey)
            val keySpec = PKCS8EncodedKeySpec(privateKeyDer)
            val keyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePrivate(keySpec)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun String.encrypt(): String {
        return try {
            if (this.isEmpty()) return ""
            val cipher =
                Cipher.getInstance("RSA/ECB/PKCS1Padding") // Using the same padding as Node.js
            cipher.init(Cipher.ENCRYPT_MODE, getPublicKeyFromDer(myPublicKey))
            val encryptedBytes = cipher.doFinal(this.toByteArray())
            Base64Utils.encode(encryptedBytes).trimIndent().replace("\n", "")
        } catch (e: Exception) {
            Log.e(TAG, "Encryption error: ${e.message}")
            this
        }
    }

    fun String.decrypt(): String {
        return try {
            if (this.isEmpty()) return ""
            val cipher =
                Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding") // Using the same padding as Node.js
            cipher.init(Cipher.DECRYPT_MODE, getPrivateKeyFromDer(myPrivateKey))
            val decryptedBytes =
                cipher.doFinal(Base64Utils.decode(this.trimIndent().replace("\n", "")))
            String(decryptedBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption error: ${e.message}")
            this
        }
    }
}