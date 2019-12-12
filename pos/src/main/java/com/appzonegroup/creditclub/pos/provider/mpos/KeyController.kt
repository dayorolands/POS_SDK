package com.appzonegroup.creditclub.pos.provider.mpos

import android.annotation.SuppressLint
import android.util.Log
import java.security.Key
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPublicKey
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class KeyController {
    private var privateKey: PrivateKey? = null
    var privateKeyEncoded: ByteArray? = null
        private set

    var publicKey: PublicKey? = null
        private set

    @Throws(Exception::class)
    private fun generateKey(passwordValueByteArray: ByteArray): Key {
        return SecretKeySpec(passwordValueByteArray, ALGORITHM_AES)
    }

    @SuppressLint("NewApi")
    fun decrypt(encryptedValue: ByteArray?): String? {
        return try {
//            val privateKey = KeyFactory.getInstance(ALGORITHM_RSA)
//                .generatePrivate(PKCS8EncodedKeySpec(privateKeyEncoded))
            val cipher = Cipher.getInstance(ALGORITHM_RSA_ECB_PKCS1PADDING)
            cipher.init(2, privateKey)
            String(cipher.doFinal(encryptedValue))
        } catch (e: Exception) {
            Log.e("Exception {}", e.stackTrace.toString())
            null
        }
    }

    @SuppressLint("NewApi")
    fun encrypt(value: ByteArray?): String? {
        return try {
            val cipher = Cipher.getInstance(ALGORITHM_RSA_ECB_PKCS1PADDING)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            String(cipher.doFinal(value))
        } catch (e: Exception) {
            Log.e("Exception {}", e.stackTrace.toString())
            null
        }
    }

    fun generatePrivateAndPublicKeys(): String? {
        val publicKeyEncoded: String? = null
        return try {
            val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_RSA)
            keyPairGenerator.initialize(2048)
            val keyPair = keyPairGenerator.genKeyPair()
//            android.util.Base64.encodeToString(keyPair.public.encoded, 0)
            privateKeyEncoded = keyPair.private.encoded
            privateKey = keyPair.private
            val modulus = (keyPair.public as RSAPublicKey).modulus.toByteArray()
            val publicKey = ByteArray(modulus.size - 1)
            System.arraycopy(modulus, 1, publicKey, 0, publicKey.size)
            byteToHex(publicKey)
        } catch (e: Exception) {
            publicKeyEncoded
        }
    }

    private fun byteToHex(bytes: ByteArray): String {
        var str: String
        val str2 = ""
        val sb = StringBuilder("")
        for (b in bytes) {
            val strHex = Integer.toHexString(b.toInt() and 255)
            str = if (strHex.length == 1) {
                "0$strHex"
            } else {
                strHex
            }
            sb.append(str)
        }
        return sb.toString().trim { it <= ' ' }
    }

    private fun ttkey(): String {
        return try {
            val gen = KeyPairGenerator.getInstance(ALGORITHM_RSA)
            gen.initialize(2048)
            val modulus = (gen.genKeyPair().public as RSAPublicKey).modulus.toByteArray()
            val publicKey = ByteArray(modulus.size - 1)
            System.arraycopy(modulus, 1, publicKey, 0, publicKey.size)
            byteToHex(publicKey)
        } catch (e: Exception) {
            ""
        }
    }

    companion object {
        private const val ALGORITHM_AES = "AES"
        private const val ALGORITHM_AES_CBC_NOPADDING = "AES/CBC/NoPadding"
        private const val ALGORITHM_RSA = "RSA"
        private const val ALGORITHM_RSA_ECB_PKCS1PADDING = "RSA/ECB/PKCS1Padding"
        private const val KEY_SIZE = 2048
    }
}