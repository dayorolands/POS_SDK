package com.dspread.qpos

import android.util.Log
import com.creditclub.core.util.safeRun
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import javax.crypto.Cipher

internal class KeyController {

    private val keyPair by lazy {
        val keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM_RSA)
        keyPairGenerator.initialize(KEY_SIZE)
        keyPairGenerator.genKeyPair()
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

    val publicKeyHex: String?
        get() = safeRun {
            val modulus = (keyPair.public as RSAPublicKey).modulus.toByteArray()
            val publicKeyBytes = ByteArray(modulus.size - 1)
            System.arraycopy(modulus, 1, publicKeyBytes, 0, publicKeyBytes.size)

            byteToHex(publicKeyBytes)
        }.data

    private fun byteToHex(bytes: ByteArray): String {
        var str: String
        val sb = StringBuilder("")

        for (b in bytes) {
            val strHex = Integer.toHexString(b.toInt() and 255)
            str = (if (strHex.length == 1) "0$strHex" else strHex)
            sb.append(str)
        }

        return sb.toString().trim { it <= ' ' }
    }

    companion object {
        private const val ALGORITHM_RSA = "RSA"
        private const val KEY_SIZE = 2048
    }
}