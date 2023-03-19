package com.cluster.core.util

import com.cluster.core.util.XORorAndorOR.XORorANDorORfunction
import okhttp3.internal.toHexString
import java.io.ByteArrayOutputStream
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec

class PinBlockHelper {
    fun getSessionKey(IPEK: String, KSN: String): String {
        var initialIPEK: String = IPEK
        val ksn = KSN.padStart(20, '0')
        var sessionkey = ""
        //Get ksn with a zero counter by ANDing it with 0000FFFFFFFFFFE00000
        val newKSN = XORorANDorORfunction(ksn, "0000FFFFFFFFFFE00000", "&")
        val counterKSN = ksn.substring(ksn.length - 5).padStart(16, '0')
        //get the number of binary associated with the counterKSN number
        var newKsnToLeft16 = newKSN.substring(newKSN.length - 16)
        val counterKSNbin = Integer.toBinaryString(counterKSN.toInt())
        var binaryCount = counterKSNbin
        for (i in 0 until counterKSNbin.length) {
            val len: Int = binaryCount.length
            var result = ""
            if (binaryCount.substring(0, 1) == "1") {
                result = "1".padEnd(len, '0')
                binaryCount = binaryCount.substring(1)
            } else {
                binaryCount = binaryCount.substring(1)
                continue
            }
            val counterKSN2 = Integer.toHexString(Integer.parseInt(result, 2)).uppercase().padStart(16, '0')
            val newKSN2 = XORorANDorORfunction(newKsnToLeft16, counterKSN2, "|")
            sessionkey = blackBoxLogic(newKSN2, initialIPEK) //Call the Black Box from here
            newKsnToLeft16 = newKSN2
            initialIPEK = sessionkey
        }
        return XORorANDorORfunction(sessionkey, "00000000000000FF00000000000000FF", "^")
    }

    private fun blackBoxLogic(ksn: String, iPek: String): String {
        if (iPek.length < 32) {
            val msg = XORorANDorORfunction(iPek, ksn, "^")
            val desResult = desEncrypt(msg, iPek)
            val rSessionKey = XORorANDorORfunction(desResult, iPek, "^")
            return rSessionKey
        }
        val currentSk = iPek
        val ksn_mod = ksn
        val leftIpek =
            XORorANDorORfunction(
                currentSk,
                "FFFFFFFFFFFFFFFF0000000000000000",
                "&"
            ).substring(16)
        val rightIpek =
            XORorANDorORfunction(
                currentSk,
                "0000000000000000FFFFFFFFFFFFFFFF",
                "&"
            ).substring(16)
        val message = XORorANDorORfunction(rightIpek, ksn_mod, "^")
        val desResult = desEncrypt(message, leftIpek)
        val rightSessionKey = XORorANDorORfunction(desResult, rightIpek, "^")
        val resultCurrentSk =
            XORorANDorORfunction(
                currentSk,
                "C0C0C0C000000000C0C0C0C000000000",
                "^"
            )
        val leftIpek2 = XORorANDorORfunction(
            resultCurrentSk,
            "FFFFFFFFFFFFFFFF0000000000000000",
            "&"
        ).substring(0, 16)
        val rightIpek2 = XORorANDorORfunction(
            resultCurrentSk,
            "0000000000000000FFFFFFFFFFFFFFFF",
            "&"
        ).substring(16)
        val message2 = XORorANDorORfunction(rightIpek2, ksn_mod, "^")
        val desResult2 = desEncrypt(message2, leftIpek2)
        val leftSessionKey = XORorANDorORfunction(desResult2, rightIpek2, "^")
        return leftSessionKey + rightSessionKey
    }

    fun desEncryptDukpt(workingKey: String, clearPinBlock: String): String {
        val pinBlock = XORorANDorORfunction(workingKey, clearPinBlock, "^")
        val keyData = hexStringToByteArray(workingKey)
        val bout = ByteArrayOutputStream()
        try {
            val keySpec: KeySpec = DESKeySpec(keyData)
            val key: SecretKey = SecretKeyFactory.getInstance("DES").generateSecret(keySpec)
            val cipher: Cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            bout.write(cipher.doFinal(hexStringToByteArray(pinBlock)))
        } catch (e: Exception) {
            println("Exception .. " + e.message)
        }
        return XORorANDorORfunction(
            workingKey, byteArrayToHexString(bout.toByteArray()).substring(
                0,
                16
            ), "^"
        )
    }

    fun generateTransKsn(ksn: String): String? {
        var ksn = ksn
        val ksnVal = ksn.substring(0, ksn.length - 5)
        val counter = ksn.substring(ksn.length - 5).toLong()
        ksn = ksnVal + counter.toHexString().padStart(5, '0')
        return ksn.padStart(20, '0').substring(4)
    }

    private fun desEncrypt(desData: String, key: String): String {
        val keyData = hexStringToByteArray(key)
        val bout = ByteArrayOutputStream()
        try {
            val keySpec: KeySpec = DESKeySpec(keyData)
            val key: SecretKey = SecretKeyFactory.getInstance("DES").generateSecret(keySpec)
            val cipher: Cipher = Cipher.getInstance("DES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            bout.write(cipher.doFinal(hexStringToByteArray(desData)))
        } catch (e: Exception) {
            print("Exception DES Encryption.. " + e.printStackTrace())
        }
        return byteArrayToHexString(bout.toByteArray()).substring(0, 16)
    }

    private fun hexStringToByteArray(key: String) : ByteArray {
        var result:ByteArray = ByteArray(0)
        for (i in 0 until key.length step 2) {
            result += Integer.parseInt(key.substring(i, (i + 2)), 16).toByte()
        }
        return result
    }

    private fun byteArrayToHexString(key: ByteArray) : String {
        var st = ""
        for (b in key) {
            st += String.format("%02X", b)
        }
        return st
    }
}