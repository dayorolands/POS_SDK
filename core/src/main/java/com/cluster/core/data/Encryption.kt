package com.cluster.core.data

import android.util.Log
import org.bouncycastle.crypto.BlockCipher
import org.bouncycastle.crypto.BufferedBlockCipher
import org.bouncycastle.crypto.engines.DESedeEngine
import org.bouncycastle.crypto.modes.CBCBlockCipher
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.util.encoders.Hex
import java.util.*

object Encryption {
    @JvmStatic
    fun encrypt(value: String?): String? {
        value ?: return null

        val tp = TripleDES()
        return tp.encryptData(value)
    }

    @JvmStatic
    fun decrypt(value: String?): String? {
        Log.e("ToDecrypt", value + "Other")
        value ?: return null

        val tp = TripleDES()
        return tp.decryptData(value)
    }

    @JvmStatic
    fun generateSessionId(phoneno: String): String {
        return "$phoneno${Date().time}"
    }

    internal class TripleDES {

        private var _keyString = "B738C0DB478907CAE98CF476"
        private var engineBlockCipher: BlockCipher? = DESedeEngine()
        private var cipher: BufferedBlockCipher = PaddedBufferedBlockCipher(CBCBlockCipher(engineBlockCipher!!))
        private var key: ByteArray = _keyString.toByteArray()

        fun encryptData(text: String?): String? {
            var cipherText: String? = null
            if (text != null) {
                cipher.init(true, KeyParameter(key))
                val input = text.toByteArray()
                val output = ByteArray(cipher.getOutputSize(input.size))
                val len = cipher.processBytes(input, 0, input.size, output, 0)
                try {
                    cipher.doFinal(output, len)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

                cipherText = String(Hex.encode(output))
            }
            return cipherText
        }

        fun decryptData(cipherText: String?): String? {
            var text: String? = null
            if (cipherText != null) {
                cipher.init(false, KeyParameter(key))
                val input = Hex.decode(cipherText.toByteArray())
                val output = ByteArray(cipher.getOutputSize(input.size))
                val len = cipher.processBytes(input, 0, input.size, output, 0)
                try {
                    cipher.doFinal(output, len)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }

                text = String(output).trim { it <= ' ' }
            }
            return text
        }
    }

}
