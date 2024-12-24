//package com.example.link
//
//import android.security.keystore.KeyGenParameterSpec
//import android.security.keystore.KeyProperties
//import java.security.KeyStore
//import javax.crypto.Cipher
//import java.security.KeyPairGenerator
//import java.security.PrivateKey
//import java.security.PublicKey
//import android.util.Base64
//
//class EncryptDecryptRSA {
//    private val KEY_ALIAS = "MyRSAKeyAlias"
//    fun isKeyPairGenerated(): Boolean {
//        return try {
//            val keyStore = KeyStore.getInstance("AndroidKeyStore")
//            keyStore.load(null)
//            val privateKey = keyStore.getKey(KEY_ALIAS, null)
//            privateKey != null
//        } catch (e: Exception) {
//            false
//        }
//    }
//
//    fun generateKeyPair() {
//        if (isKeyPairGenerated()) {
//            println("RSA Key Pair already exists. Skipping key generation.")
//            return
//        }
//
//        val keyPairGenerator =
//            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
//        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
//            KEY_ALIAS,
//            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
//        )
//            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
//            .setKeySize(2048)
//            .build()
//
//        keyPairGenerator.initialize(keyGenParameterSpec)
//        keyPairGenerator.generateKeyPair()
//
//        println("RSA Key Pair generated and stored in Keystore.")
//    }
//
//
//    fun getPublicKey(): PublicKey {
//        val keyStore = KeyStore.getInstance("AndroidKeyStore")
//        keyStore.load(null)
//        return keyStore.getCertificate(KEY_ALIAS).publicKey
//    }
//
//
//    fun getPrivateKey(): PrivateKey {
//        val keyStore = KeyStore.getInstance("AndroidKeyStore")
//        keyStore.load(null)
//        return keyStore.getKey(KEY_ALIAS, null) as PrivateKey
//    }
//
//
//    fun encryptData(plaintext: ByteArray): ByteArray {
//        val publicKey = getPublicKey()
//        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
//        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
//        return cipher.doFinal(plaintext)
//    }
//
//
//    fun decryptData(ciphertext: ByteArray): ByteArray {
//        val privateKey = getPrivateKey()
//        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
//        cipher.init(Cipher.DECRYPT_MODE, privateKey)
//        return cipher.doFinal(ciphertext)
//    }
//
//    // Decode Base64 and decrypt the data
//    fun decryptDataFromBase64(base64String: String): String {
//        val encryptedData = Base64.decode(base64String, Base64.DEFAULT)
//        val decryptedBytes = decryptData(encryptedData)
//        return String(decryptedBytes)
//    }
//}