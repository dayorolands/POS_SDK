package com.cluster.pos.utils

import android.annotation.SuppressLint
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESedeKeySpec

inline val ByteArray.asDesEdeKey: SecretKey
    @Throws(
        InvalidKeyException::class,
        NoSuchAlgorithmException::class,
        InvalidKeySpecException::class,
    )
    get() {
        val rawKey = this
        val keyDes = if (rawKey.size == 8) {
            val keyDes = ByteArray(16)
            System.arraycopy(rawKey, 0, keyDes, 0, 8)
            System.arraycopy(rawKey, 0, keyDes, 8, 8)

            keyDes
        } else {
            val keyDes = ByteArray(24)
            System.arraycopy(rawKey, 0, keyDes, 0, 16)
            System.arraycopy(rawKey, 0, keyDes, 16, 8)

            keyDes
        }

        val keySpec = DESedeKeySpec(keyDes)
        val keyFactory = SecretKeyFactory.getInstance("DESede")
        return keyFactory.generateSecret(keySpec)
    }

@SuppressLint("GetInstance")
fun SecretKey.encrypt(data: ByteArray, transformation: String = "DESede/ECB/NoPadding"): ByteArray {
    val cipher = Cipher.getInstance(transformation)
    cipher.init(Cipher.ENCRYPT_MODE, this)
    return cipher.doFinal(data)
}

@SuppressLint("GetInstance")
fun SecretKey.decrypt(data: ByteArray, transformation: String = "DESede/ECB/NoPadding"): ByteArray {
    val cipher = Cipher.getInstance(transformation)
    cipher.init(Cipher.DECRYPT_MODE, this)
    return cipher.doFinal(data)
}
