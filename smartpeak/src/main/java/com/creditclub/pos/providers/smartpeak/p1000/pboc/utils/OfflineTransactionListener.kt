package com.creditclub.pos.providers.smartpeak.p1000.pboc.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import com.creditclub.pos.PosManager.SessionData
import com.basewin.aidl.OnPBOCListener
import com.basewin.aidl.OnPinInputListener
import com.basewin.commu.Commu
import com.basewin.commu.define.CommuListener
import com.basewin.commu.define.CommuParams
import com.basewin.commu.define.CommuStatus
import com.basewin.commu.define.CommuType
import com.basewin.define.*
import com.basewin.packet8583.exception.Packet8583Exception
import com.basewin.services.ServiceManager
import com.basewin.utils.BCDHelper
import com.creditclub.core.ui.widget.DialogOptionItem
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.pos.providers.smartpeak.p1000.pboc.iso8583.Iso8583Mgr
import com.creditclub.pos.providers.smartpeak.p1000.pboc.pinpad.OnPinDialogListener
import com.creditclub.pos.providers.smartpeak.p1000.pboc.pinpad.PinInputDialog
import com.creditclub.pos.providers.smartpeak.p1000.pinpad.PinpadInterfaceVersion
import com.creditclub.pos.providers.smartpeak.p1000.utils.GlobalData
import java.io.UnsupportedEncodingException

/**
 * PBOC监听过程(PBOC listener process)
 */
class OfflineTransactionListener(
    private val ba: Context,
    private val dialogProvider: DialogProvider,
    private val sessionData: SessionData
) : OnPBOCListener {
    private var pindialog: PinInputDialog? = null
    private var mIso8583Mgr: Iso8583Mgr? = null
    private var commu: Commu? = null
    private var sendData: ByteArray? = null
    private var keylayout = ByteArray(96)
    private val receiveData: ByteArray? = null
    private var cardtype = 0
    private var cardno: String? = null

    //pinpad the callback(pinpad的回调)
    private val pinpadListener: OnPinInputListener =
        object : OnPinInputListener {
            @Throws(RemoteException::class)
            override fun onInput(len: Int, key: Int) {
                //returns the pinpad the length of the input, the key is invalid(返回pinpad输入中的长度，Key无效)
                Log.d(
                    "TransactionListener",
                    "Pinpad password length in the display:$len"
                )
                val message = Message()
                message.what = PIN_SHOW
                val bundle = Bundle()
                bundle.putInt("len", len)
                bundle.putInt("key", key)
                message.data = bundle
                pinpad_model.sendMessage(message)
            }

            @Throws(RemoteException::class)
            override fun onError(errorCode: Int) {
                //pinpad result to error(pinpad出错)
                Log.d("TransactionListener", "Pinpad error code:$errorCode")
                pinpad_model.sendEmptyMessage(PIN_DIALOG_DISMISS)
            }

            override fun onConfirm(
                data: ByteArray,
                isNonePin: Boolean
            ) {
                //the user to identify the input password,this Data is cryptography encrypted to the password(用户确定了输入的密码,Data为加密了的密码密文)
                if (!isNonePin) {
                    //Encrypted transaction(加密交易)
                    Log.d(
                        "TransactionListener",
                        "Pinpad enter password over,encrypt data:" + BCDHelper.hex2DebugHexString(
                            data,
                            data.size
                        )
                    )
                    pinpad_model.sendEmptyMessage(PIN_DIALOG_DISMISS)
                    if (cardtype == CardType.IC_CARD) {
                        try {
                            ServiceManager.getInstence().pboc
                                .comfirmPinpad(data)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        online_transaction.sendEmptyMessage(ONLINE_PROCESS_COMMU)
                    }
                } else {
                    //no secret trading(无密交易)
                    Log.d("TransactionListener", "Pinpad not encrypt transaction")
                    pinpad_model.sendEmptyMessage(PIN_DIALOG_DISMISS)
                    if (cardtype == CardType.IC_CARD) {
                        try {
                            ServiceManager.getInstence().pboc
                                .comfirmPinpad(null)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        online_transaction.sendEmptyMessage(ONLINE_PROCESS_COMMU)
                    }
                }
            }

            @Throws(RemoteException::class)
            override fun onCancel() {
                //if you click on the cancel button(点了取消按钮)
                Log.d("TransactionListener", "Pinpad User cancel")
                pinpad_model.sendEmptyMessage(PIN_DIALOG_DISMISS)
            }

            @Throws(RemoteException::class)
            override fun onPinpadShow(data: ByteArray) {
                //result Key values,use this on setting the pinpad layout(从底层返回键值，使用此去设置密码键盘)
                Log.d(
                    "TransactionListener",
                    "Pinpad data is enter password coordinate values"
                )
                val message = Message()
                message.what = SETLAYOUT
                message.obj = data
                pinpad_model.sendMessage(message)
            }
        }

    //pinpad process control(pinpad流程控制)
    @SuppressLint("HandlerLeak")
    private val pinpad_model: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                PIN_DIALOG_SHOW -> {
                    //display pinpad(显示pinpad)
                    Log.d("TransactionListener", "Pinpad show")
                    try {
                        //set the pinpad display mode(设置pinpad显示模式)
                        ServiceManager.getInstence().pinpad
                            .setPinpadMode(GlobalDef.MODE_RANDOM)
                    } catch (e1: Exception) {
                        e1.printStackTrace()
                    }

                    //Pinpad parameter settings[context,card no,tips,amount,callback](pinpad参数设置[上下文,卡号,提示,金额,回调])
                    pindialog = PinInputDialog(
                        ba,
                        cardno,
                        "Please enter the Bank card password",
                        "${sessionData.amount}",
                        object : OnPinDialogListener {
                            override fun OnPinInput(result: Int) {}
                            override fun OnCreateOver() {
                                Log.d(
                                    "TransactionListener",
                                    "Pinpad View create success"
                                )
                                sendEmptyMessage(GETLAYOUT)
                            }
                        })
                }
                PIN_DIALOG_DISMISS -> {
                    //close pinpad(关闭pinpad)
                    Log.d("TransactionListener", "Pinpad Close")
                    if (pindialog != null) {
                        pindialog!!.dismiss()
                        pindialog = null
                    }
                }
                PIN_SHOW -> {
                    //according to the length of the input password(显示输入的密码长度)
                    Log.d("TransactionListener", "Pinpad display password length")
                    if (pindialog != null) {
                        val bundle = msg.data
                        pindialog!!.setPins(bundle.getInt("len"), bundle.getInt("key"))
                    }
                }
                SETLAYOUT -> {
                    //set layout(设置布局)
                    Log.d(
                        "TransactionListener",
                        "Pinpad start set view" + BCDHelper.bcdToString(
                            msg.obj as ByteArray,
                            0,
                            (msg.obj as ByteArray).size
                        )
                    )
                    pindialog!!.setKeyShow(
                        msg.obj as ByteArray
                    ) {
                        keylayout = pindialog!!.keyLayout
                        Log.d(
                            "TransactionListener",
                            "Pinpad start setting view:" + BCDHelper.bcdToString(
                                keylayout,
                                0,
                                keylayout.size
                            )
                        )
                        try {
                            ServiceManager.getInstence().pinpad
                                .setPinpadLayout(keylayout)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                GETLAYOUT ->                     //get layout(获取布局)
                    try {
                        ServiceManager.getInstence().pinpad
                            .setOnPinInputListener(pinpadListener)
                        when (GlobalData.getInstance().pinpadVersion) {
                            PinpadInterfaceVersion.PINPAD_INTERFACE_VERSION1 -> ServiceManager.getInstence()
                                .pinpad.inputOnlinePin(cardno, byteArrayOf(0, 6, 12))
                            PinpadInterfaceVersion.PINPAD_INTERFACE_VERSION2 -> ServiceManager.getInstence()
                                .pinpad.inputOnlinePinNew(
                                    GlobalData.getInstance().tmkId,
                                    cardno,
                                    byteArrayOf(0, 6, 12)
                                )
                            PinpadInterfaceVersion.PINPAD_INTERFACE_VERSION3 -> ServiceManager.getInstence()
                                .pinpad.inputOnlinePinByArea(
                                    GlobalData.getInstance().area,
                                    GlobalData.getInstance().tmkId,
                                    cardno,
                                    byteArrayOf(0, 6, 12)
                                )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
            }
        }
    }
    private val mCommuListener: CommuListener = object : CommuListener {
        override fun OnStatus(
            paramInt: Int,
            paramArrayOfByte: ByteArray
        ) {
            when (paramInt) {
                CommuStatus.INIT_COMMU -> {
                    Log.d(
                        "TransactionListener",
                        "PBOC Communication init:" + CommuStatus.getStatusMsg(CommuStatus.INIT_COMMU)
                    )
                    dialogProvider.showProgressBar("commu init...")
                }
                CommuStatus.CONNECTING -> {
                    Log.d(
                        "TransactionListener",
                        "PBOC Communication connecting:" + CommuStatus.getStatusMsg(CommuStatus.CONNECTING)
                    )
                    dialogProvider.showProgressBar("commu connecting...")
                }
                CommuStatus.SENDING -> {
                    Log.d(
                        "TransactionListener",
                        "PBOC Communication sending:" + CommuStatus.getStatusMsg(CommuStatus.SENDING)
                    )
                    dialogProvider.showProgressBar("commu send data...")
                }
                CommuStatus.RECVING -> {
                    Log.d(
                        "TransactionListener",
                        "PBOC Communication recving:" + CommuStatus.getStatusMsg(CommuStatus.RECVING)
                    )
                    dialogProvider.showProgressBar("commu recv data...")
                }
                CommuStatus.FINISH -> {
                    Log.d(
                        "TransactionListener",
                        "PBOC Communication finish:" + CommuStatus.getStatusMsg(CommuStatus.FINISH)
                    )
                    dialogProvider.showProgressBar("commu finish...")
                    System.arraycopy(
                        paramArrayOfByte,
                        0,
                        receiveData,
                        0,
                        paramArrayOfByte.size
                    )
                    online_transaction.sendEmptyMessage(ONLINE_PROCESS_FINISH)
                }
                else -> {
                }
            }
        }

        override fun OnError(paramInt: Int, paramString: String) {
            Log.d(
                "TransactionListener",
                "PBOC Communication error code:$paramInt error:$paramString"
            )
            dialogProvider.showProgressBar("commu finish...")
            online_transaction.sendEmptyMessage(ONLINE_PROCESS_FINISH)
        }
    }

    //online trading the process(在线支付过程)
    @SuppressLint("ShowToast", "HandlerLeak")
    private val online_transaction: Handler = object : Handler() {
        @SuppressLint("ShowToast", "HandlerLeak")
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                ONLINE_PROCESS_COMMU -> {
                    //ISO8583 processing the start(ISO流程开始)
                    dialogProvider.showProgressBar("commu with server...")
                    Thread(Runnable {
                        mIso8583Mgr = Iso8583Mgr(ba)
                        Log.d("TransactionListener", "PBOC ISO8583 encode")
                        try {
                            //packaging(封装)
                            sendData = mIso8583Mgr!!.packData()
                        } catch (e: Packet8583Exception) {
                            e.printStackTrace()
                        } catch (e: UnsupportedEncodingException) {
                            e.printStackTrace()
                        }
                        commu = Commu.getInstence()

                        //If you do not use in the configuration file configuration, dynamic configuration will be used to code(如果不使用配置文件中的配置，将使用代码动态配置)
                        Log.d(
                            "TransactionListener",
                            "PBOC Communication no configuration file"
                        )
                        commu?.setCommuParams(params)
                        commu?.dataCommu(ba, sendData, mCommuListener)
                    }).start()
                }
                ONLINE_PROCESS_FINISH -> {
                    //ISO8583 processing the end(ISO8583流程结束)
                    Log.d("TransactionListener", "PBOC Communication over decode data")
                    object : Thread() {
                        override fun run() {
                            if (mIso8583Mgr == null) {
                                mIso8583Mgr = Iso8583Mgr(ba)
                            }
                            //parsing(解析)
//                            mIso8583Mgr.unpackData(receiveData);
                            Log.d(
                                "TransactionListener",
                                "PBOC Communication decode:" + mIso8583Mgr!!.getBitData(3)
                            )
                            dialogProvider.hideProgressBar()
                            Log.d("TransactionListener", "commu finish!")
                        }
                    }.start()
                }
                else -> {
                }
            }
        }
    }

    //Dynamic setting configuration file(动态设置配置文件)
    private val params: CommuParams
        get() {
            //Dynamic setting configuration file(动态设置配置文件)
            val params = CommuParams()
            params.ip = "140.206.168.98"
            params.type = CommuType.SOCKET
            params.port = 4900
            params.timeout = 5
            return params
        }

    @Throws(RemoteException::class)
    override fun onStartPBOC() {
        //PBOC process the start(PBOC流程开始)
        Log.d("TransactionListener", "PBOC Start")
        dialogProvider.showProgressBar("start pboc...")
    }

    override fun onRequestAmount() {
        //if you don't set the amount before,can be in this setting(如果之前没有设置金额，可以再次设置)
        Log.d("TransactionListener", "PBOC Setting amount")
        dialogProvider.hideProgressBar()
        Looper.prepare()
        try {
            ServiceManager.getInstence().pboc
                .setAmount(java.lang.String.valueOf(sessionData.amount).toInt())
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Looper.loop()
    }

    override fun onSelectApplication(applicationList: List<String>) {
        //selection card application(在此选着卡应用)
        Log.d("TransactionListener", "PBOC Select Application")
        dialogProvider.hideProgressBar()
        Looper.prepare()
        val options = applicationList.map { DialogOptionItem(it) }
        dialogProvider.showOptions("Please chose application", options) {
            onSubmit { i ->
                try {
                    ServiceManager.getInstence().pboc.selectApplication(i + 1)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        Looper.loop()
    }

    override fun onFindingCard(cardType: Int, data: Intent) {
        //find and identify the card as well as read relevant data(寻卡和选卡，然后读取相关数据)
        Log.d("TransactionListener", "PBOC Finding Choose card")
        dialogProvider.showProgressBar("finding card...")
        when (cardType) {
            CardType.MAG_CARD -> {
                cardtype = CardType.MAG_CARD
                Log.d("TransactionListener", "PBOC CardType:Mag card")
                //MAG card data entity class
                val magCardInfo = OutputMagCardInfo(data)
                Log.d(
                    "TransactionListener",
                    "PBOC Mag card number:" + magCardInfo.pan
                )
                Log.d(
                    "TransactionListener",
                    "PBOC Mag card track 2:" + magCardInfo.track2HexString
                )
                Log.d(
                    "TransactionListener",
                    "PBOC Mag card track 3:" + magCardInfo.track3HexString
                )
                Log.d(
                    "TransactionListener",
                    "PBOC Term of validity:" + magCardInfo.expiredDate
                )
                Log.d(
                    "TransactionListener",
                    "PBOC Service Code: " + magCardInfo.serviceCode
                )
                cardno = magCardInfo.pan
                pinpad_model.sendEmptyMessage(PIN_DIALOG_SHOW)
            }
            CardType.IC_CARD -> {
                cardtype = CardType.IC_CARD
                Log.d("TransactionListener", "PBOC CardType:IC card")
            }
            CardType.RF_CARD -> {
                cardtype = CardType.RF_CARD
                Log.d("TransactionListener", "PBOC CardType:RF card")
            }
        }
    }

    @Throws(RemoteException::class)
    override fun onRequestInputPIN(
        isOnlinePin: Boolean,
        retryTimes: Int
    ) {
        // Need a password,At this point you need to call password pinpad(底层返回需要设置密码，这个时候需要调用pinpad模块进行密码输入，只有IC PBOC流程)
        Log.d("TransactionListener", "PBOC Request input PIN")
        pinpad_model.sendEmptyMessage(PIN_DIALOG_SHOW)
    }

    override fun onConfirmCardInfo(info: Intent) {
        //may need to confirm the IC card information display interface(确认IC卡卡号信息的时候，可能需要进行界面显示，此处略过，最后确认完了调用confirmCardInfo()即可)
        Log.d("TransactionListener", "PBOC Confirm Card Info")
        dialogProvider.hideProgressBar()
        val out = OutputCardInfoData(info)
        Log.d("TransactionListener", "IC card SN:" + out.cardSN)
        Log.d("TransactionListener", "IC card number:" + out.pan)
        Log.d("TransactionListener", "IC card expired date:" + out.expiredDate)
        Log.d("TransactionListener", "IC card service code:" + out.serviceCode)
        Log.d("TransactionListener", "IC card track:" + out.track)
        cardno = out.pan
        Looper.prepare()
        dialogProvider.confirm("Confirm card number", cardno) {
            onSubmit {
                try {
                    ServiceManager.getInstence().pboc.confirmCardInfo()
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
        Looper.loop()
    }

    @Throws(RemoteException::class)
    override fun onConfirmCertInfo(
        certType: String,
        certInfo: String
    ) {
        //confirm the identity(确认身份信息)
        Log.d("TransactionListener", "PBOC Confirm credentials info")
    }

    @Throws(RemoteException::class)
    override fun onAARequestOnlineProcess(actionAnalysisData: Intent) {
        //online trading(联机交易)
        Log.d("TransactionListener", "PBOC the Online trade process")
        val out = OutputPBOCAAData(actionAnalysisData)
        Log.d("TransactionListener", "PBOC 55 field:" + out.get55Field())
        Log.d("TransactionListener", "PBOC AA result:" + out.aaResult)
        Log.d("TransactionListener", "PBOC Card seq number:" + out.cardSeqNum)
        Log.d("TransactionListener", "PBOC IC data:" + out.icData)
        Log.d("TransactionListener", "PBOC reversal data:" + out.reversalData)
        Log.d("TransactionListener", "PBOC TC:" + out.tcData)

        //jump the ISO8583 to encapsulate(跳转到ISO8583封装)
        online_transaction.sendMessage(online_transaction.obtainMessage(ONLINE_PROCESS_COMMU))
    }

    @Throws(RemoteException::class)
    override fun onTransactionResult(result: Int, data: Intent) {
        //Transaction result(交易结果)
        Log.d("TransactionListener", "PBOC the Transaction result")
        dialogProvider.hideProgressBar()
        when (result) {
            PBOCTransactionResult.QPBOC_ARQC -> {
                //quick pay to process(快速交易流程)
                val rf_data = OutputQPBOCResult(data)
                val field55String = rf_data.get55Field()
                val pan = rf_data.pan
                cardno = rf_data.pan
                val maskedpan = rf_data.maskedPan
                val trackString = rf_data.track
                Log.d("TransactionListener", "PBOC Trade result track 2:$trackString")
                val bcdTrack =
                    BCDHelper.StrToBCD(trackString, trackString.length)
                Log.d(
                    "TransactionListener",
                    "PBOC Trade result track 2 the bcd:" + BCDHelper.hex2DebugHexString(
                        bcdTrack,
                        bcdTrack.size
                    )
                )
                val expiredate = rf_data.expiredDate
                Log.d("TransactionListener", "PBOC call PinPad")
                pinpad_model.sendEmptyMessage(PIN_DIALOG_SHOW)
            }
            PBOCTransactionResult.APPROVED -> {
                //normal pay to process(普通交易流程)
                try {
                    Log.d(
                        "TransactionListener",
                        "PBOC EC balance：" + ServiceManager.getInstence().pboc
                            .readEcBalance()
                    )
                    val data1 =
                        ServiceManager.getInstence().pboc
                            .getEmvTlvData(0x9F5D)
                    if (data1 != null) {
                        Log.d(
                            "TransactionListener",
                            "data 1:" + BCDHelper.hex2DebugHexString(
                                data1,
                                data1.size
                            )
                        )
                    }
                    val data2 =
                        ServiceManager.getInstence().pboc
                            .getEmvTlvData(0x9F79)
                    if (data2 != null) {
                        Log.d(
                            "TransactionListener",
                            "data 2:" + BCDHelper.hex2DebugHexString(
                                data2,
                                data2.size
                            )
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            PBOCTransactionResult.TERMINATED -> {
                //transaction the fail as well as stopping the transaction(交易失败并且停止交易)
                Log.d("TransactionListener", "PBOC Transaction fail")
            }
        }
    }

    @Throws(RemoteException::class)
    override fun onReadECBalance(ecBalance: Intent) {
        //online trading the balance,but temporarily didn't use him(在线余额，暂时没有使用)
        Log.d("TransactionListener", "PBOC EC balance")
        dialogProvider.hideProgressBar()
        val balanceout = OutputECBalance(ecBalance)
        Log.d("TransactionListener", "EC balance:" + balanceout.ecBalance)
        Log.d(
            "TransactionListener",
            "EC currency code:" + balanceout.currencyType
        )
        Looper.prepare()
        dialogProvider.confirm("ec balance", balanceout.ecBalance.toString()) {}
        Looper.loop()
    }

    @Throws(RemoteException::class)
    override fun onReadCardOfflineRecord(contents: Intent) {
        //offline trading the balance,but temporarily didn't use him(离线余额，暂时没有使用)
        Log.d("TransactionListener", "PBOC Transaction record")
        dialogProvider.hideProgressBar()
        val list = OutputOfflineRecord(contents)
        val record = arrayOfNulls<String>(list.recordSize)
        for (i in 0 until list.recordSize) {
            Log.d("TransactionListener", "Record " + (i + 1) + ":" + list.getRecord(i))
            record[i] = list.getRecord(i)
        }
        Looper.prepare()
//        EnterDialog(ba).showListDialog("record list", record)
        Looper.loop()
    }

    @Throws(RemoteException::class)
    override fun onError(result: Intent) {
        //PBOC process to error(流程出错)
        Log.d("TransactionListener", "PBOC Error")
        dialogProvider.hideProgressBar()
    }

    @Throws(RemoteException::class)
    override fun onRequestSinature() {
        // TODO Auto-generated method stub
    }

    companion object {
        private const val ONLINE_PROCESS_COMMU = 1
        private const val ONLINE_PROCESS_FINISH = 2

        private const val PIN_DIALOG_SHOW = 1 //display pinpad(Pinpad弹出)
        private const val PIN_DIALOG_DISMISS = 2 //close pinpad(Pinpad关闭)
        private const val PIN_SHOW = 3 //display inputting the pinpad(PIN输入值的显示)
        private const val SETLAYOUT = 4 //set key layout the pinpad(设置keys布局)
        private const val GETLAYOUT = 5 //get key layout the pinpad(获取keys布局)
    }
}