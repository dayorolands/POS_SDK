package com.dspread.qpos

import com.creditclub.pos.R
import com.dspread.xpos.EmvAppTag
import com.dspread.xpos.QPOSService.EMVDataOperation


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 07/01/2020.
 * Appzone Ltd
 */
class QPosHandler(private val qPosManager: QPosManager) {
    val pos get()= qPosManager.pos

//    fun handle(code:Int) {
//        when (code) {
//            8003 -> {
//                try {
//                    Thread.sleep(200)
//                } catch (e: InterruptedException) {
//                    e.printStackTrace()
//                }
//                var content = ""
//                content = if (nfcLog == null) {
//                    val h: Hashtable<String, String> = pos.getNFCBatchData()
//                    val tlv = h["tlv"]
//                    TRACE.i("nfc batchdata1: $tlv")
//                    statusEditText.getText().toString() + "\nNFCbatchData: " + h["tlv"]
//                } else {
//                    statusEditText.getText().toString() + "\nNFCbatchData: " + nfcLog
//                }
//                statusEditText.setText(content)
//            }
//            1701 -> {
//                updateEMVCfgByXML()
//                if (appList == null) {
//                    Toast.makeText(this@MainActivity, "File read failed", Toast.LENGTH_SHORT).show()
//                    ConfigUtil.putReadXmlStatus(this@MainActivity, false)
//                    return
//                }
//                if (appList.size < 1) {
//                    Toast.makeText(
//                        this@MainActivity,
//                        R.string.updateEMVAppStatus,
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    statusEditText.setText(R.string.updateEMVAppStatus)
//                    return
//                }
//                val tagApp: TagApp = appList.get(0)
//                val emvApp =
//                    ArrayList<String?>()
//                val appLen =
//                    if (BaseTag.TAG_APP.length > tagApp.datasLength) tagApp.datasLength else BaseTag.TAG_APP.length
//                var i = 0
//                while (i < appLen) {
//                    val data = tagApp.getData(i)
//                    if (TextUtils.isEmpty(data)) {
//                        i++
//                        continue
//                    }
//                    if (data!!.contains(EmvAppTag.Currency_conversion_factor)) {
//                        i++
//                        continue
//                    }
//                    emvApp.add(data)
//                    i++
//                }
//                pos.updateEmvAPP(EMVDataOperation.Add, emvApp)
//            }
//            1702 -> {
//                updateEMVCfgByXML()
//                if (capkList == null) {
//                    Toast.makeText(this@MainActivity, "File read failed", Toast.LENGTH_SHORT).show()
//                    ConfigUtil.putReadXmlStatus(this@MainActivity, false)
//                    return
//                }
//                if (capkList.size < 1) {
//                    Toast.makeText(
//                        this@MainActivity,
//                        R.string.updateEMVCapkStatus,
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    statusEditText.setText(R.string.updateEMVCapkStatus)
//                    return
//                }
//                val tagCapk: TagCapk = capkList.get(0)
//                val emvCapk =
//                    ArrayList<String?>()
//                val caLen =
//                    if (BaseTag.TAG_APP.length > tagCapk.datasLength) tagCapk.datasLength else BaseTag.TAG_APP.length
//                var i = 0
//                while (i < caLen) {
//                    emvCapk.add(tagCapk.getData(i))
//                    i++
//                }
//                pos.updateEmvCAPK(EMVDataOperation.Add, emvCapk)
//            }
//            1703 -> {
//                //                    statusEditText.setText(clearPubKeyModel);
//                val keyIndex: Int = getKeyIndex()
//                var digEnvelopStr: String? = null
//                var posKeys: Poskeys? = null
//                try {
//                    if (resetIpekFlag) {
//                        posKeys = DukptKeys()
//                    }
//                    if (resetMasterKeyFlag) {
//                        posKeys = TMKKey()
//                    }
//                    posKeys!!.rsA_public_key = pubModel //Model of device public key
//                    digEnvelopStr = Envelope.getDigitalEnvelopStrByKey(
//                        getAssets().open("priva.pem"),
//                        posKeys, Poskeys.RSA_KEY_LEN.RSA_KEY_1024, keyIndex
//                    )
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//                pos.udpateWorkKey(digEnvelopStr)
//            }
//            else -> {
//            }
//        }
//    }
}