package com.creditclub.pos.utils

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESedeKeySpec

/**
 * Created by mac on 1/29/19.
 */

class TripleDesCipher @Throws(Exception::class)
constructor(rawkey: ByteArray) {
    internal var key: SecretKey

    init {
        key = readKey(rawkey)
    }

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

        val keyspec = DESedeKeySpec(keyDes)
        val keyfactory = SecretKeyFactory.getInstance("DESede")
        return keyfactory.generateSecret(keyspec)
    }

    @Throws(Exception::class)
    fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("DESede/ECB/NoPadding")
        //final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(plain)
    }

    @Throws(Exception::class)
    fun decrypt(cipher: ByteArray): ByteArray {
        val dcipher = Cipher.getInstance("DESede/ECB/NoPadding")
        //final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
        dcipher.init(Cipher.DECRYPT_MODE, key)
        return dcipher.doFinal(cipher)
    }
}
