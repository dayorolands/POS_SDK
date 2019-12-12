package com.appzonegroup.creditclub.pos.provider.mpos

import android.util.Log
import java.security.Key
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class KeyController {
//    var privateKey: PrivateKey? = null
//        private set
//
//    var publicKey: PublicKey? = null
//        private set

    val keyPair by lazy {
        val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_RSA)
        keyPairGenerator.initialize(2048)
        keyPairGenerator.genKeyPair()
    }

    @Throws(Exception::class)
    private fun generateKey(passwordValueByteArray: ByteArray): Key {
        return SecretKeySpec(passwordValueByteArray, ALGORITHM_AES)
    }

    fun decrypt(encryptedValue: ByteArray?): String? {
        return try {
            val cipher = Cipher.getInstance(ALGORITHM_RSA)
            cipher.init(2, keyPair.private)
            byteToHex(cipher.doFinal(encryptedValue))
        } catch (e: Exception) {
            Log.e("Exception {}", e.stackTrace.toString())
            null
        }
    }

    fun encrypt(value: ByteArray?): ByteArray? {
        return try {
            val cipher = Cipher.getInstance(ALGORITHM_RSA)
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.public)
            cipher.doFinal(value)
        } catch (e: Exception) {
            Log.e("Exception {}", e.stackTrace.toString())
            null
        }
    }

    fun generatePrivateAndPublicKeys(): String? {
        return try {
            android.util.Base64.encodeToString(keyPair.public.encoded, 0)
//            privateKey = keyPair.private
//            publicKey = keyPair.public
            val modulus = (keyPair.public as RSAPublicKey).modulus.toByteArray()
            val publicKey = ByteArray(modulus.size - 1)
            System.arraycopy(modulus, 1, publicKey, 0, publicKey.size)

            byteToHex(publicKey)
        } catch (e: Exception) {
            null
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