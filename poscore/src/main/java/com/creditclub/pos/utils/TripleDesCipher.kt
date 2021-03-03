package com.creditclub.pos.utils

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESedeKeySpec

/**
 * Created by mac on 1/29/19.
 */

inline class TripleDesCipher(private val rawkey: ByteArray) {
    @Throws(Exception::class)
    fun readKey(rawkey: ByteArray): SecretKey {
        val keyDes = if (rawkey.size == 8) {
            val keyDes = ByteArray(16)
            System.arraycopy(rawkey, 0, keyDes, 0, 8)
            System.arraycopy(rawkey, 0, keyDes, 8, 8)

            keyDes
        } else  {
            val keyDes = ByteArray(24)
            System.arraycopy(rawkey, 0, keyDes, 0, 16)
            System.arraycopy(rawkey, 0, keyDes, 16, 8)

            keyDes
        }

        val keySpec = DESedeKeySpec(keyDes)
        val keyFactory = SecretKeyFactory.getInstance("DESede")
        return keyFactory.generateSecret(keySpec)
    }

    @Throws(Exception::class)
    fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("DESede/ECB/NoPadding")
        //final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        cipher.init(Cipher.ENCRYPT_MODE, readKey(rawkey))
        return cipher.doFinal(plain)
    }

    @Throws(Exception::class)
    fun decrypt(encryptedData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("DESede/ECB/NoPadding")
        //final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        cipher.init(Cipher.DECRYPT_MODE, readKey(rawkey))
        return cipher.doFinal(encryptedData)
    }
}
