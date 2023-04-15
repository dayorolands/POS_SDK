package com.dspread.qpos

import android.text.TextUtils
import com.dspread.qpos.utils.DUKPK2009_CBC
import java.util.*

fun extractMcrData(decodeData: Hashtable<String, String>) {
    val formatID = decodeData["formatID"]
    if (formatID == "31" || formatID == "40" || formatID == "37" || formatID == "17" || formatID == "11" || formatID == "10") {
        val maskedPAN = decodeData["maskedPAN"]
        val expiryDate = decodeData["expiryDate"]
        val cardHolderName = decodeData["cardholderName"]
        val serviceCode = decodeData["serviceCode"]
        val trackblock = decodeData["trackblock"]
        val psamId = decodeData["psamId"]
        val posId = decodeData["posId"]
        val pinblock = decodeData["pinblock"]
        val macblock = decodeData["macblock"]
        val activateCode = decodeData["activateCode"]
        val trackRandomNumber = decodeData["trackRandomNumber"]
    } else if (formatID == "FF") {
        val type = decodeData["type"]
        val encTrack1 = decodeData["encTrack1"]
        val encTrack2 = decodeData["encTrack2"]
        val encTrack3 = decodeData["encTrack3"]
    } else {
        val orderID = decodeData["orderId"]
        val maskedPAN = decodeData["maskedPAN"]
        val expiryDate = decodeData["expiryDate"]
        val cardHolderName = decodeData["cardholderName"]
        //					String ksn = decodeData.get("ksn");
        val serviceCode = decodeData["serviceCode"]
        val track1Length = decodeData["track1Length"]
        val track2Length = decodeData["track2Length"]
        val track3Length = decodeData["track3Length"]
        val encTracks = decodeData["encTracks"]
        val encTrack1 = decodeData["encTrack1"]
        val encTrack2 = decodeData["encTrack2"]
        val encTrack3 = decodeData["encTrack3"]
        val partialTrack = decodeData["partialTrack"]
        // TODO
        val pinKsn = decodeData["pinKsn"]
        val trackksn = decodeData["trackksn"]
        val pinBlock = decodeData["pinBlock"]
        val encPAN = decodeData["encPAN"]
        val trackRandomNumber = decodeData["trackRandomNumber"]
        val pinRandomNumber = decodeData["pinRandomNumber"]
        var realPan: String? = null
        if (!TextUtils.isEmpty(trackksn) && !TextUtils.isEmpty(
                encTrack2
            )
        ) {
            val clearPan: String = DUKPK2009_CBC.getData(
                trackksn,
                encTrack2,
                DUKPK2009_CBC.Enum_key.DATA,
                DUKPK2009_CBC.Enum_mode.CBC
            )
            realPan = clearPan.substring(0, maskedPAN!!.length)
        }
        if (!TextUtils.isEmpty(pinKsn) && !TextUtils.isEmpty(
                pinBlock
            ) && !TextUtils.isEmpty(realPan)
        ) {
            val date: String = DUKPK2009_CBC.getData(
                pinKsn,
                pinBlock,
                DUKPK2009_CBC.Enum_key.PIN,
                DUKPK2009_CBC.Enum_mode.CBC
            )
            val parsCarN =
                "0000" + realPan!!.substring(realPan.length - 13, realPan.length - 1)
            val s: String = DUKPK2009_CBC.xor(parsCarN, date)
        }
    }
}