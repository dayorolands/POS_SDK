package com.appzonegroup.creditclub.pos.provider.qpos

import android.content.Context
import android.media.AudioManager
import com.appzonegroup.creditclub.pos.R
import java.io.*
import kotlin.experimental.and
import kotlin.experimental.xor

object QPOSUtil {

    private infix fun Byte.and(num: Int): Byte = this and num.toByte()
    private infix fun Byte.shr(num: Int): Int = this.toInt() shr num
    private infix fun Byte.shl(num: Int): Int = this.toInt() shl num
    private infix fun Int.or(byte: Byte): Int = this or byte.toInt()

    const val HEXES = "0123456789ABCDEF"
    @JvmStatic
    fun byteArray2Hex(raw: ByteArray?): String? {
        if (raw == null) {
            return null
        }
        val hex = StringBuilder(2 * raw.size)
        for (b in raw) {
            hex.append(HEXES[b and 0xF0 shr 4]).append(HEXES[(b and 0x0F).toInt()])
        }
        return hex.toString()
    }

    //将hex值转为ascii码
    @JvmStatic
    fun convertHexToString(hex: String): String {
        val sb = StringBuilder()
        val temp = StringBuilder()
        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        var i = 0
        while (i < hex.length - 1) {
            //grab the hex in pairs
            val output = hex.substring(i, i + 2)
            //convert hex to decimal
            val decimal = output.toInt(16)
            //convert the decimal to character
            sb.append(decimal.toChar())
            temp.append(decimal)
            i += 2
        }
        return sb.toString()
    }

    /**
     * 16进制格式的字符串转成16进制byte 44 --> byte 0x44
     *
     * @param hexString
     * @return
     */
    @JvmStatic
    fun HexStringToByteArray(hexString: String?): ByteArray { //
        var hexString = hexString
        if (hexString == null || hexString == "") {
            return byteArrayOf()
        }
        if (hexString.length == 1 || hexString.length % 2 != 0) {
            hexString = "0$hexString"
        }
        hexString = hexString.toUpperCase()
        val length = hexString.length / 2
        val hexChars = hexString.toCharArray()
        val d = ByteArray(length)
        for (i in 0 until length) {
            val pos = i * 2
            d[i] = (charToByte(hexChars[pos]) shl 4 or charToByte(hexChars[pos + 1])).toByte()
        }
        return d
    }

    private fun charToByte(c: Char): Byte {
        return "0123456789ABCDEF".indexOf(c).toByte()
    }

    /**
     * 中文字符串转成16进制数组
     *
     * @param str
     * @return
     */
    fun CNToHex(str: String): ByteArray? { // String string = "";
// for (int i = 0; i < str.length(); i++) {
// String s = String.valueOf(str.charAt(i));
// byte[] bytes = null;
// try {
// bytes = s.getBytes("gbk");
// } catch (UnsupportedEncodingException e) {
// e.printStackTrace();
// }
// for (int j = 0; j < bytes.length; j++) {
// string += Integer.toHexString(bytes[j] & 0xff);
// }
// }
        var b: ByteArray? = null
        try {
            b = str.toByteArray(charset("GBK"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return b
    }

    /**
     * byte 转换成16进制格式字符串显示
     *
     * @param b
     * @return
     */
    fun getHexString(b: ByteArray): String {
        val result = StringBuffer("")
        for (i in b.indices) {
            result.append(
                "0x" + Integer.toString(
                    (b[i] and 0xff) + 0x100,
                    16
                ).substring(1) + ","
            )
        }
        return result.substring(0, result.length - 1)
    }

    /**
     * int转成16进制的byte
     *
     * @param i
     * @return
     */
    fun IntToHex(i: Int): ByteArray {
        var string: String? = null
        string = if (i >= 0 && i < 10) {
            "0$i"
        } else {
            Integer.toHexString(i)
        }
        return HexStringToByteArray(string)
    }

    /**
     * 将指定byte数组以16进制的形式打印到控制台
     *
     * @param b
     */
    fun printHexString(b: ByteArray) {
        for (i in b.indices) {
            var hex = Integer.toHexString((b[i] and 0xFF).toInt())
            if (hex.length == 1) {
                hex = "0$hex"
            }
            print(hex.toUpperCase())
        }
    }

    /**
     * 把16进制字节转换成int
     *
     * @param b
     * @return
     */
    fun byteArrayToInt(b: ByteArray): Int {
        var result = 0
        for (i in b.indices) {
            result = result shl 8
            result = result or (b[i] and 0xff) //
        }
        return result
    }

    /**
     * 异或输入字节流
     *
     * @param b
     * @param startPos
     * @param Len
     * @return
     */
    fun XorByteStream(b: ByteArray, startPos: Int, Len: Int): Byte {
        var bRet: Byte = 0x00
        for (i in 0 until Len) {
            bRet = bRet xor b[startPos + i]
        }
        return bRet
    }
    /**
     * Gets the subarray of length <tt>length</tt> from <tt>array</tt> that
     * starts at <tt>offset</tt>.
     */
    /**
     * Gets the subarray from <tt>array</tt> that starts at <tt>offset</tt>.
     */
    @JvmOverloads
    operator fun get(
        array: ByteArray,
        offset: Int,
        length: Int = array.size - offset
    ): ByteArray {
        val result = ByteArray(length)
        System.arraycopy(array, offset, result, 0, length)
        return result
    }

    fun turnUpVolume(context: Context, factor: Int) {
        val sv: Int
        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        sv = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            sv * factor / 10,
            AudioManager.FLAG_PLAY_SOUND
        )
    }

    /**
     * 将毫秒值转换成时分秒（时长）
     *
     * @param l
     * @return
     */
    fun formatLongToTimeStr(l: Long, context: Context): String? {
        var str = ""
        var hour: Long = 0
        var minute: Long = 0
        var second = 0f
        second = l.toFloat() / 1000.toFloat()
        if (second > 60) {
            minute = (second / 60).toLong()
            second = second % 60
            if (minute > 60) {
                hour = minute / 60
                minute = minute % 60
                str =
                    ("" + hour + context.resources.getString(R.string.hours) + minute + context.resources.getString(
                        R.string.minute
                    ) + second
                            + context.resources.getString(R.string.seconds))
            } else {
                str =
                    "" + minute + context.resources.getString(R.string.minute) + second + context.resources.getString(
                        R.string.seconds
                    )
            }
        } else {
            str = "" + second + context.resources.getString(R.string.seconds)
        }
        return str
    }

    fun bcd2asc(src: ByteArray): ByteArray? {
        val results = ByteArray(src.size * 2)
        for (i in src.indices) { // 高Nibble转换
            if (src[i] and 0xF0 shr 4 <= 9) {
                results[2 * i] = ((src[i] and 0xF0 shr 4) + 0x30).toByte()
            } else {
                results[2 * i] = ((src[i] and 0xF0 shr 4) + 0x37).toByte() // 大写A~F
            }
            // 低Nibble转换
            if (src[i] and 0x0F <= 9) {
                results[2 * i + 1] = ((src[i] and 0x0F) + 0x30).toByte()
            } else {
                results[2 * i + 1] = ((src[i] and 0x0F) + 0x37).toByte() // 大写A~F
            }
        }
        return results
    }

    fun ecb(`in`: ByteArray): ByteArray? {
        var a1 = ByteArray(8)
        for (i in 0 until `in`.size / 8) {
            val temp = ByteArray(8)
            System.arraycopy(`in`, i * 8, temp, 0, temp.size)
            a1 = xor8(a1, temp)
        }
        if (`in`.size % 8 != 0) {
            val temp = ByteArray(8)
            System.arraycopy(
                `in`,
                `in`.size / 8 * 8,
                temp,
                0,
                `in`.size - `in`.size / 8 * 8
            )
            a1 = xor8(a1, temp)
        }
        return bcd2asc(a1)
    }

    fun xor8(src1: ByteArray, src2: ByteArray): ByteArray {
        val results = ByteArray(8)
        for (i in results.indices) {
            results[i] = (src1[i] xor src2[i]) as Byte
        }
        return results
    }

    @Throws(Exception::class)
    fun readRSAStream(`in`: InputStream?): String {
        return try {
            val br = BufferedReader(InputStreamReader(`in`))
            var line: String? = null
            val sb = StringBuilder()
            while (br.readLine().also { line = it } != null) {
                if (line!!.contains("BEGIN")) {
                    sb.delete(0, sb.length)
                } else {
                    if (line!!.contains("END")) {
                        break
                    }
                    sb.append(line)
                    sb.append('\r')
                }
            }
            sb.toString()
        } catch (var5: IOException) {
            throw Exception("鍏\ue104挜鏁版嵁娴佽\ue1f0鍙栭敊锟�?")
        } catch (var6: NullPointerException) {
            throw Exception("鍏\ue104挜杈撳叆娴佷负锟�?")
        }
    }

    @JvmStatic
    fun checkStringAllZero(str: String): Boolean {
        if (str.startsWith("0")) return true
        var result = true
        //        Integer.MAX_VALUE  4个字节
//       long MAX_VALUE = 0x7fffffffffffffffL;
        val byteCou = str.length / 2
        val count: Int
        count = if (byteCou % 4 == 0) {
            byteCou / 4
        } else {
            byteCou / 4 + 1
        }
        var sub: String? = null
        for (i in 0 until count) {
            sub = if (i == count - 1) {
                str.substring(i * 8, sub!!.length)
            } else {
                str.substring(i * 8, (i + 1) * 8)
            }
            val l = sub.toLong(16)
            if (l > 0) {
                result = false
                break
            }
        }
        return result
    }
}