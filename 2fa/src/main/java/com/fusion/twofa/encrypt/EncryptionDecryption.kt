package com.fusion.twofa.encrypt

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.io.ByteArrayInputStream
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.CertificateFactory
import javax.crypto.Cipher

object ExtEncryptionDecryption {

    private const val KEY_ALIAS = "MyRSAKeyAlias"
    var mPublicKeyPEM: String? = null // Store the public key in PEM format
    var mPrivateKey: PrivateKey? = null

    init {
        if (isKeyPairGenerated()) {
            Log.d("EncryptDecryptRSA", "RSA Key Pair already exists. Skipping key generation.")
        } else {
            generateKeyPair()
        }
        val publicKey = getPublicKey()
        mPrivateKey = getPrivateKey()

        // Convert the public key to PEM format and store it
        mPublicKeyPEM = publicKeyToPEM(publicKey)
        Log.d("PublicKey", "Public Key PEM: \n$mPublicKeyPEM")
    }

    private fun isKeyPairGenerated(): Boolean {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val privateKey = keyStore.getKey(KEY_ALIAS, null)
            privateKey != null
        } catch (e: Exception) {
            false
        }
    }


    private fun generateKeyPair() {
        val keyPairGenerator =
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
            .setKeySize(2048)
            .build()

        keyPairGenerator.initialize(keyGenParameterSpec)
        keyPairGenerator.generateKeyPair()
        Log.d("EncryptDecryptRSA", "RSA Key Pair generated and stored in Keystore.")
    }

    private fun getPublicKey(): PublicKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        return keyStore.getCertificate(KEY_ALIAS).publicKey
    }

    private fun getPrivateKey(): PrivateKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        return keyStore.getKey(KEY_ALIAS, null) as PrivateKey
    }

    // Convert PublicKey to PEM format
    private fun publicKeyToPEM(publicKey: PublicKey): String {
        val encoded = Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
        return "-----BEGIN PUBLIC KEY-----\n$encoded\n-----END PUBLIC KEY-----"
    }

    // Convert PEM to PublicKey
    private fun pemToPublicKey(pem: String): PublicKey {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val cleanedPem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\n", "")
        val decodedBytes = Base64.decode(cleanedPem, Base64.DEFAULT)
        val certInputStream = ByteArrayInputStream(decodedBytes)
        val cert = certificateFactory.generateCertificate(certInputStream)
        return cert.publicKey
    }

    // Extension function for encryption
    fun ByteArray.encryptData(): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, pemToPublicKey(mPublicKeyPEM!!))
        return cipher.doFinal(this)
    }

    // Extension function for decryption
    fun ByteArray.decryptData(): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, mPrivateKey)
        return cipher.doFinal(this)
    }

    // Decode Base64 and decrypt the data
    fun decryptDataFromBase64(base64String: String): String {
        val encryptedData = Base64.decode(base64String, Base64.DEFAULT)
        val decryptedBytes = encryptedData.decryptData()
        return String(decryptedBytes)
    }
}
