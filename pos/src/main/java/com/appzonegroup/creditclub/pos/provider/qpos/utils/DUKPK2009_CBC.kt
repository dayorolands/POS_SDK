package com.appzonegroup.creditclub.pos.provider.qpos.utils

import java.lang.reflect.Modifier
import java.security.Key
import java.util.*
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESedeKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

object DUKPK2009_CBC {
    /*
    * ksnV:ksn
    * datastrV:data
    * Enum_key:Encryption/Decryption
    * Enum_mode
    *
    * */
    fun getDate(
        ksnV: String?,
        datastrV: String?,
        key: Enum_key?,
        mode: Enum_mode
    ): String {
        return getDate(
            ksnV,
            datastrV,
            key,
            mode,
            null
        )
    }

    fun getDate(
        ksnV: String?,
        datastrV: String?,
        key: Enum_key?,
        mode: Enum_mode,
        clearIpek: String?
    ): String { //		// TODO Auto-generated method stub
        var ipek: ByteArray? = null
        val byte_ksn =
            parseHexStr2Byte(ksnV)
        ipek = if (clearIpek == null || clearIpek.length == 0) {
            val bdk = "0123456789ABCDEFFEDCBA9876543210"
            val byte_bdk =
                parseHexStr2Byte(
                    bdk
                )
            GenerateIPEK(
                byte_ksn,
                byte_bdk
            )
        } else {
            parseHexStr2Byte(
                clearIpek
            )
        }
        val ipekStr =
            parseByte2HexStr(ipek) // 经测试 ipek都一样
        println("ipekStr=$ipekStr")
        val dataKey =
            GetDataKey(
                byte_ksn,
                ipek
            )
        val dataKeyStr =
            parseByte2HexStr(
                dataKey
            )
        println("dataKeyStr=$dataKeyStr")
        val dataKeyVariant =
            GetDataKeyVariant(
                byte_ksn,
                ipek
            )
        val dataKeyStrVariant =
            parseByte2HexStr(
                dataKeyVariant
            )
        println("dataKeyStrVariant=$dataKeyStrVariant")
        val pinKey =
            GetPinKeyVariant(
                byte_ksn,
                ipek
            )
        val pinKeyStr =
            parseByte2HexStr(
                pinKey
            )
        println("pinKeyStr=$pinKeyStr")
        val macKey =
            GetMacKeyVariant(
                byte_ksn,
                ipek
            )
        val macKeyStr =
            parseByte2HexStr(
                macKey
            )
        println("macKeyStr=$macKeyStr")
        var keySel: String? = null
        when (key) {
            Enum_key.MAC -> keySel =
                macKeyStr
            Enum_key.PIN -> keySel =
                pinKeyStr
            Enum_key.DATA -> keySel =
                dataKeyStr
            Enum_key.DATA_VARIANT -> keySel =
                dataKeyStrVariant
        }
        var buf: ByteArray? = null
        if (mode == Enum_mode.CBC) buf =
            TriDesDecryptionCBC(
                parseHexStr2Byte(
                    keySel
                ),
                parseHexStr2Byte(
                    datastrV
                )
            ) else if (mode == Enum_mode.ECB) buf =
            TriDesDecryptionECB(
                parseHexStr2Byte(
                    keySel
                ),
                parseHexStr2Byte(
                    datastrV
                )
            )
        //        System.out.println("data: " + deResultStr);
        return parseByte2HexStr(buf)
    }

    fun GenerateIPEK(ksn: ByteArray?, bdk: ByteArray?): ByteArray {
        val result: ByteArray
        val temp: ByteArray
        var temp2: ByteArray?
        val keyTemp: ByteArray
        result = ByteArray(16)
        temp = ByteArray(8)
        keyTemp = ByteArray(16)
        //        Array.Copy(bdk, keyTemp, 16);
        System.arraycopy(bdk, 0, keyTemp, 0, 16) //Array.Copy(bdk, keyTemp, 16);
        //        Array.Copy(ksn, temp, 8);
        System.arraycopy(ksn, 0, temp, 0, 8) //Array.Copy(ksn, temp, 8);
        temp[7] = temp[7] and 0xE0.toByte()
        //        TDES_Enc(temp, keyTemp, out temp2);
        temp2 = TriDesEncryption(
            keyTemp,
            temp
        ) //TDES_Enc(temp, keyTemp, out temp2);temp
        //        Array.Copy(temp2, result, 8);
        System.arraycopy(temp2, 0, result, 0, 8) //Array.Copy(temp2, result, 8);
        keyTemp[0] = keyTemp[0] xor 0xC0.toByte()
        keyTemp[1] = keyTemp[1] xor 0xC0.toByte()
        keyTemp[2] = keyTemp[2] xor 0xC0.toByte()
        keyTemp[3] = keyTemp[3] xor 0xC0.toByte()
        keyTemp[8] = keyTemp[8] xor 0xC0.toByte()
        keyTemp[9] = keyTemp[9] xor 0xC0.toByte()
        keyTemp[10] = keyTemp[10] xor 0xC0.toByte()
        keyTemp[11] = keyTemp[11] xor 0xC0.toByte()
        //        TDES_Enc(temp, keyTemp, out temp2);
        temp2 = TriDesEncryption(
            keyTemp,
            temp
        ) //TDES_Enc(temp, keyTemp, out temp2);
        //        Array.Copy(temp2, 0, result, 8, 8);
        System.arraycopy(temp2, 0, result, 8, 8) //Array.Copy(temp2, 0, result, 8, 8);
        return result
    }

    fun GetDUKPTKey(
        ksn: ByteArray?,
        ipek: ByteArray?
    ): ByteArray { //    	System.out.println("ksn===" + parseByte2HexStr(ksn));
        val key: ByteArray
        val cnt: ByteArray
        val temp: ByteArray
        //    	byte shift;
        var shift: Int
        key = ByteArray(16)
        //        Array.Copy(ipek, key, 16);
        System.arraycopy(ipek, 0, key, 0, 16)
        temp = ByteArray(8)
        cnt = ByteArray(3)
        cnt[0] = (ksn!![7] and 0x1F) as Byte
        cnt[1] = ksn[8]
        cnt[2] = ksn[9]
        //        Array.Copy(ksn, 2, temp, 0, 6);
        System.arraycopy(ksn, 2, temp, 0, 6)
        temp[5] = temp[5] and 0xE0.toByte()
        shift = 0x10
        while (shift > 0) {
            if (cnt[0] and shift.toByte() > 0) { //            	System.out.println("**********");
                temp[5] = temp[5] or shift.toByte()
                NRKGP(key, temp)
            }
            shift = shift shr 1
        }
        shift = 0x80
        while (shift > 0) {
            if (cnt[1] and shift.toByte() > 0) { //            	System.out.println("&&&&&&&&&&");
                temp[6] = temp[6] or shift.toByte()
                NRKGP(key, temp)
            }
            shift = shift shr 1
        }
        shift = 0x80
        while (shift > 0) {
            if (cnt[2] and shift.toByte() > 0) { //            	System.out.println("^^^^^^^^^^");
                temp[7] = temp[7] or shift.toByte()
                NRKGP(key, temp)
            }
            shift = shift shr 1
        }
        return key
    }

    /// <summary>
/// Non Reversible Key Generatino Procedure
/// private function used by GetDUKPTKey
/// </summary>
    private fun NRKGP(key: ByteArray, ksn: ByteArray) {
        val temp: ByteArray
        var key_l: ByteArray
        var key_r: ByteArray
        val key_temp: ByteArray
        var i: Int
        temp = ByteArray(8)
        key_l = ByteArray(8)
        key_r = ByteArray(8)
        key_temp = ByteArray(8)
        //        Console.Write("");
//        Array.Copy(key, key_temp, 8);
        System.arraycopy(key, 0, key_temp, 0, 8)
        i = 0
        while (i < 8) {
            temp[i] = (ksn[i] xor key[8 + i]) as Byte
            i++
        }
        //        DES_Enc(temp, key_temp, out key_r);
        key_r = TriDesEncryption(
            key_temp,
            temp
        )!!
        i = 0
        while (i < 8) {
            key_r[i] = key_r[i] xor key[8 + i]
            i++
        }
        key_temp[0] = key_temp[0] xor 0xC0.toByte()
        key_temp[1] = key_temp[1] xor 0xC0.toByte()
        key_temp[2] = key_temp[2] xor 0xC0.toByte()
        key_temp[3] = key_temp[3] xor 0xC0.toByte()
        key[8] = key[8] xor 0xC0.toByte()
        key[9] = key[9] xor 0xC0.toByte()
        key[10] = key[10] xor 0xC0.toByte()
        key[11] = key[11] xor 0xC0.toByte()
        i = 0
        while (i < 8) {
            temp[i] = (ksn[i] xor key[8 + i]) as Byte
            i++
        }
        //        DES_Enc(temp, key_temp, out key_l);
        key_l = TriDesEncryption(
            key_temp,
            temp
        )!!
        i = 0
        while (i < 8) {
            key[i] = (key_l[i] xor key[8 + i]) as Byte
            i++
        }
        //        Array.Copy(key_r, 0, key, 8, 8);
        System.arraycopy(key_r, 0, key, 8, 8)
    }

    /// <summary>
/// Get current Data Key variant
/// Data Key variant is XOR DUKPT Key with 0000 0000 00FF 0000 0000 0000 00FF 0000
/// </summary>
/// <param name="ksn">Key serial number(KSN). A 10 bytes data. Which use to determine which BDK will be used and calculate IPEK. With different KSN, the DUKPT system will ensure different IPEK will be generated.
/// Normally, the first 4 digit of KSN is used to determine which BDK is used. The last 21 bit is a counter which indicate the current key.</param>
/// <param name="ipek">IPEK (16 byte).</param>
/// <returns>Data Key variant (16 byte)</returns>
    fun GetDataKeyVariant(ksn: ByteArray?, ipek: ByteArray?): ByteArray {
        val key: ByteArray
        key =
            GetDUKPTKey(ksn, ipek)
        key[5] = key[5] xor 0xFF.toByte()
        key[13] = key[13] xor 0xFF.toByte()
        return key
    }

    /// <summary>
/// Get current PIN Key variant
/// PIN Key variant is XOR DUKPT Key with 0000 0000 0000 00FF 0000 0000 0000 00FF
/// </summary>
/// <param name="ksn">Key serial number(KSN). A 10 bytes data. Which use to determine which BDK will be used and calculate IPEK. With different KSN, the DUKPT system will ensure different IPEK will be generated.
/// Normally, the first 4 digit of KSN is used to determine which BDK is used. The last 21 bit is a counter which indicate the current key.</param>
/// <param name="ipek">IPEK (16 byte).</param>
/// <returns>PIN Key variant (16 byte)</returns>
    fun GetPinKeyVariant(ksn: ByteArray?, ipek: ByteArray?): ByteArray {
        val key: ByteArray
        key =
            GetDUKPTKey(ksn, ipek)
        key[7] = key[7] xor 0xFF.toByte()
        key[15] = key[15] xor 0xFF.toByte()
        return key
    }

    fun GetMacKeyVariant(ksn: ByteArray?, ipek: ByteArray?): ByteArray {
        val key: ByteArray
        key =
            GetDUKPTKey(ksn, ipek)
        key[6] = key[6] xor 0xFF.toByte()
        key[14] = key[14] xor 0xFF.toByte()
        return key
    }

    fun GetDataKey(ksn: ByteArray?, ipek: ByteArray?): ByteArray? {
        val temp1 =
            GetDataKeyVariant(
                ksn,
                ipek
            )
        return TriDesEncryption(
            temp1,
            temp1
        )
    }

    // 3DES加密
    fun TriDesEncryption(byteKey: ByteArray, dec: ByteArray?): ByteArray? {
        try {
            var en_key = ByteArray(24)
            if (byteKey.size == 16) {
                System.arraycopy(byteKey, 0, en_key, 0, 16)
                System.arraycopy(byteKey, 0, en_key, 16, 8)
            } else if (byteKey.size == 8) {
                System.arraycopy(byteKey, 0, en_key, 0, 8)
                System.arraycopy(byteKey, 0, en_key, 8, 8)
                System.arraycopy(byteKey, 0, en_key, 16, 8)
            } else {
                en_key = byteKey
            }
            val key =
                SecretKeySpec(en_key, "DESede")
            val ecipher =
                Cipher.getInstance("DESede/ECB/NoPadding")
            ecipher.init(Cipher.ENCRYPT_MODE, key)
            // Encrypt
            // String en_txt = parseByte2HexStr(en_b);
// String en_txt =byte2hex(en_b);
            return ecipher.doFinal(dec)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    // 3DES解密 CBC
    fun TriDesDecryptionCBC(byteKey: ByteArray?, dec: ByteArray?): ByteArray? {
        var en_key: ByteArray? = ByteArray(24)
        if (byteKey!!.size == 16) {
            System.arraycopy(byteKey, 0, en_key, 0, 16)
            System.arraycopy(byteKey, 0, en_key, 16, 8)
        } else if (byteKey.size == 8) {
            System.arraycopy(byteKey, 0, en_key, 0, 8)
            System.arraycopy(byteKey, 0, en_key, 8, 8)
            System.arraycopy(byteKey, 0, en_key, 16, 8)
        } else {
            en_key = byteKey
        }
        try {
            var deskey: Key? = null
            val keyiv = ByteArray(8)
            val spec = DESedeKeySpec(en_key)
            val keyfactory =
                SecretKeyFactory.getInstance("desede")
            deskey = keyfactory.generateSecret(spec)
            val cipher =
                Cipher.getInstance("desede" + "/CBC/NoPadding")
            val ips = IvParameterSpec(keyiv)
            cipher.init(Cipher.DECRYPT_MODE, deskey, ips)
            return cipher.doFinal(dec)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    // 3DES解密 ECB
    fun TriDesDecryptionECB(
        byteKey: ByteArray?,
        dec: ByteArray?
    ): ByteArray? { // private String TriDesDecryption(String dnc_key, byte[] dec){
// byte[] byteKey = parseHexStr2Byte(dnc_key);
        var en_key: ByteArray? = ByteArray(24)
        if (byteKey!!.size == 16) {
            System.arraycopy(byteKey, 0, en_key, 0, 16)
            System.arraycopy(byteKey, 0, en_key, 16, 8)
        } else if (byteKey.size == 8) {
            System.arraycopy(byteKey, 0, en_key, 0, 8)
            System.arraycopy(byteKey, 0, en_key, 8, 8)
            System.arraycopy(byteKey, 0, en_key, 16, 8)
        } else {
            en_key = byteKey
        }
        var key: SecretKey? = null
        key = try {
            SecretKeySpec(en_key, "DESede")
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        try {
            val dcipher =
                Cipher.getInstance("DESede/ECB/NoPadding")
            dcipher.init(Cipher.DECRYPT_MODE, key)
            // byte[] dec = parseHexStr2Byte(en_data);
// Decrypt
            // String de_txt = parseByte2HexStr(removePadding(de_b));
            return dcipher.doFinal(dec)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    // 十六进制字符串转字节数组
    fun parseHexStr2Byte(hexStr: String?): ByteArray? {
        if (hexStr!!.length < 1) return null
        val result = ByteArray(hexStr.length / 2)
        for (i in 0 until hexStr.length / 2) {
            val high = hexStr.substring(i * 2, i * 2 + 1).toInt(16)
            val low = hexStr.substring(i * 2 + 1, i * 2 + 2).toInt(
                16
            )
            result[i] = (high * 16 + low).toByte()
        }
        return result
    }

    // 字节数组转十六进制字符串
    fun parseByte2HexStr(buf: ByteArray?): String {
        val sb = StringBuffer()
        for (i in buf!!.indices) {
            var hex = Integer.toHexString((buf[i] and 0xFF.toByte()).toInt())
            if (hex.length == 1) {
                hex = "0$hex"
            }
            sb.append(hex.toUpperCase())
        }
        return sb.toString()
    }

    // 数据补位
    fun dataFill(dataStr: String): String {
        var dataStr = dataStr
        var len = dataStr.length
        if (len % 16 != 0) {
            dataStr += "80"
            len = dataStr.length
        }
        while (len % 16 != 0) {
            dataStr += "0"
            len++
            println(dataStr)
        }
        return dataStr
    }

    fun xor(key1: String?, key2: String?): String {
        var result = ""
        val arr1 =
            parseHexStr2Byte(key1)
        val arr2 =
            parseHexStr2Byte(key2)
        val arr3 = ByteArray(arr1!!.size)
        for (i in arr1.indices) {
            arr3[i] = (arr1[i] xor arr2!![i]) as Byte
        }
        result =
            parseByte2HexStr(arr3)
        return result
    }

    @Throws(InterruptedException::class)
    @JvmStatic
    fun main(args: Array<String>) { //        07-08 17:50:27.306 12376-12376/com.dspread.demoui D/POS_SDK: onRequestOnlineProcess5F201A20202020202020202020202020202020202020202020202020204F08A0000003330101015F24032612319F160F4243544553542031323334353637389F21031750229A031907089F02060000000001119F03060000000000009F34030203009F120A50424F432044454249549F0607A00000033301015F300202209F4E0F616263640000000000000000000000C408622622FFFFFF3603C10A09118012400705E00002C708DBD7F58811779698C00A09118012400705E00001C28201880C54D377643A72400707E993BDEB6AFD891CFD5EC8CA03A251DF9301E70F76999ADABCECF859C26B9320724644D15B53BDE669414C7C8336EFDC0892A6F883DB5163D0613557949D66349BB6CB6BBCD8877017D3FEF5404C4446F2F2244CB62C62CAAE6EB86F99C9F31E69DB32BBDA2390A73EA907E4D8BDEED105E876319F4D17A5DE1788B0DA32730E4102F42A7232BE4D9D5E7BF46E7313C0F190E4F7A7D320D29DD3765E06DB5FE847C8B2B5ABBBAC0B22E5C9722303EF6E1C050C33B4F88D1BE8E79A8FBACA1086E466CB79A54A528DF53D98DA85E79EACAC4F464B0BC2941A540E1E6DFA47D4D369F50BEECFDC37AED04F63500BED4D4DB524E69345F6FE94A1CB2353D39959953393ADDD7930A43E2FCC3AE8AB348B0A8025C63C8650AF6F7C2F613EEF31549B6E073898D256815A851B5C39341B609BB3DB9974985550F096DEA5440B429BB0346D93FC25A17441F27F219A4004EE2A244014434E5D17B9F645CACB534E0CF7D3D555EE861780CF33A674D0A9A04C523C85D3F8062CE34309514A32F2AA
//        String tlvDate = "5F201A20202020202020202020202020202020202020202020202020204F08A0000003330101015F24032512319F160F4243544553543132333435363738009F21031824329A031908019F02060000000011119F03060000000000009F34030203009F120A50424F432044454249549F0607A00000033301015F300202209F4E0F616263640000000000000000000000C408621691FFFFFF5696C10A00000510F3C360600001C708F6368E3D35599107C00A00000510F3C360600001C22046FF9418E43BAC13B98549CFA3C3BA60816EE7BF1F231711FD1C2F3EFD43D891D010A274BDC41868B91F5069764D5797B823";
//        String tlvDate = "5f200a46414e2f4a49554855414f07a00000000310105f24032110319f160f4243544553543132333435363738009f21030542259a037005079f02060000000011119f03060000000000009f34031f03009f120b56495341204352454449549f0607a00000000310105f300202019f4e0f616263640000000000000000000000c408451461ffffff2125c00a00000332100300e0001dc22045e76e7f539c7ae82061b909dcc05b5151210784da7fe1ad82b3b5a9fa14c6e2d0105214696f298eddf4b12519f8d185a01e";
//        List<TLV> parse = TLVParser.parse(tlvDate);
//c0
//        String onLineksn = TLVParser.searchTLV(parse, "c0").value;
//        //c2
//		String onLineblockData = TLVParser.searchTLV(parse, "c2").value;
//        //c1
//        String Pinksn = TLVParser.searchTLV(parse, "c1").value;
//        //c7
//        String pinblockData = TLVParser.searchTLV(parse, "c7").value;
//        String pin = getDate(Pinksn, pinblockData, Enum_key.PIN, Enum_mode.ECB);
//        String onLinedate = getDate(onLineksn, onLineblockData, Enum_key.DATA, Enum_mode.CBC);
//        System.out.println(onLinedate);
//        parse = TLVParser.parse(onLinedate);
//
//        String realPan = TLVParser.searchTLV(parse, "5A").value;
//		String parsCarN = "0000" + realPan.substring(realPan.length() - 13, realPan.length() - 1);
//		String realPin = xor(parsCarN, pin);
//        System.out.println(pin);
    }

    fun printArrFun(cl: Array<Any>) {
        for (i in cl.indices) {
            println(cl[i].toString())
        }
    }

    fun copyOfFun(cl: Array<Any?>, newLen: Int): Any? {
        val aClass = cl.javaClass
        if (!aClass.isArray) return null
        val componentType = aClass.componentType
        val Arr = java.lang.reflect.Array.newInstance(componentType, newLen)
        System.arraycopy(cl, 0, Arr, 0, Math.min(cl.size, newLen))
        return Arr
    }

    fun printFields(cl: Class<*>) {
        val fields = cl.declaredFields
        for (f in fields) {
            val type = f.type
            val name = f.name
            print(" ")
            val modifiers = Modifier.toString(f.modifiers)
            if (modifiers.length > 0) print("$modifiers ")
            println(type.name + " " + name + ";")
        }
    }

    enum class Enum_key {
        DATA, PIN, MAC, DATA_VARIANT
    }

    enum class Enum_mode {
        ECB, CBC
    }

    internal class TestClass(var name: String) : TimerTask() {
        var age: String? = null
        var color: String? = null
        var aint = 0
        private var aBoolean = false

        fun isaBoolean(): Boolean {
            return aBoolean
        }

        fun setaBoolean(aBoolean: Boolean) {
            this.aBoolean = aBoolean
        }

        override fun run() {
            println("dsvdsvdvssdv")
        }

    }
}