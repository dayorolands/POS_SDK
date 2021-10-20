package com.wizarpos.emvsample.db

import com.wizarpos.util.StringUtil

class CAPKTable(
    // _id
    var id: Int = -1,

    // rid
    var rID: String? = "",// Registered Application Provider Identifier

    // capki
    var capki: String? = "",// Certificate Authority Public Key Index

    // hashIndex
    var hashIndex: Byte = 0,// Hash Algorithm Indicator

    // arithIndex
    var arithIndex: Byte = 0,// Certificate Authority Public Key Algorithm Indicator

    // modul
    var modul: String? = "",// Certificate Authority Public Key Modulus

    // exponent
    var exponent: String? = "",// Certificate Authority Public Key Exponent
    private var checkSum: String? = "",// Certificate Authority Public Key Check Sum

    // expiry
    var expiry: String? = "",// Certificate Expiration Date
) {
    fun getModulLength(): Int {
        return modul!!.length
    }

    fun getExponentLength(): Int {
        return exponent!!.length
    }

    // checksum
    fun getChecksum(): String? {
        return checkSum
    }

    fun setChecksum(checksum: String?) {
        checkSum = checksum
    }

    fun getDataBuffer(): ByteArray {
        val data = ByteArray(512)
        var offset = 0
        data[offset] = 0x9F.toByte()
        data[offset + 1] = 0x06
        data[offset + 2] = (rID!!.length / 2).toByte()
        System.arraycopy(StringUtil.hexString2bytes(rID), 0, data, offset + 3, rID!!.length / 2)
        offset += 3 + rID!!.length / 2
        data[offset] = 0x9F.toByte()
        data[offset + 1] = 0x22
        data[offset + 2] = 0x01
        data[offset + 3] = StringUtil.hexString2bytes(capki)[0]
        offset += 4
        data[offset] = 0xDF.toByte()
        data[offset + 1] = 0x05
        data[offset + 2] = expiry!!.length.toByte()
        System.arraycopy(expiry!!.toByteArray(), 0, data, offset + 3, expiry!!.length)
        offset += 3 + expiry!!.length
        data[offset] = 0xDF.toByte()
        data[offset + 1] = 0x06
        data[offset + 2] = 0x01
        data[offset + 3] = hashIndex
        offset += 4
        data[offset] = 0xDF.toByte()
        data[offset + 1] = 0x07
        data[offset + 2] = 0x01
        data[offset + 3] = arithIndex
        offset += 4
        data[offset] = 0xDF.toByte()
        data[offset + 1] = 0x02
        data[offset + 2] = 0x81.toByte()
        data[offset + 3] = (modul!!.length / 2 and 0xFF).toByte()
        System.arraycopy(StringUtil.hexString2bytes(modul), 0, data, offset + 4, modul!!.length / 2)
        offset += 4 + modul!!.length / 2
        data[offset] = 0xDF.toByte()
        data[offset + 1] = 0x04
        data[offset + 2] = (exponent!!.length / 2).toByte()
        System.arraycopy(StringUtil.hexString2bytes(exponent),
            0,
            data,
            offset + 3,
            exponent!!.length / 2)
        offset += 3 + exponent!!.length / 2
        if (checkSum != null && checkSum!!.length > 0) {
            data[offset] = 0xDF.toByte()
            data[offset + 1] = 0x03
            data[offset + 2] = (checkSum!!.length / 2).toByte()
            System.arraycopy(StringUtil.hexString2bytes(checkSum),
                0,
                data,
                offset + 3,
                checkSum!!.length / 2)
            offset += 3 + checkSum!!.length / 2
        }
        val dataOut = ByteArray(offset)
        System.arraycopy(data, 0, dataOut, 0, dataOut.size)
        return dataOut
    }
}