package com.dspread.qpos

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.pos.*
import com.creditclub.pos.card.CardData
import com.creditclub.pos.card.CardReader
import com.creditclub.pos.card.CardReaderEvent
import com.creditclub.pos.card.CardReaderEventListener
import com.creditclub.pos.printer.PosPrinter
import com.dspread.qpos.utils.FileUtils
import com.dspread.qpos.utils.QPOSUtil
import com.dspread.xpos.QPOSService
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.dsl.module
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 04/12/2019.
 * Appzone Ltd
 */
class QPosManager(activity: CreditClubActivity) : PosManager,
    DialogProvider by activity.dialogProvider, KoinComponent {

    private val keyIndex = 0
    private val parameters: PosParameter by inject()
    private val posConfig: PosConfig by inject()

    internal val pos by lazy { QPOSService.getInstance(QPOSService.CommunicationMode.BLUETOOTH); }
    internal val listener by lazy { QPosListener(this) }

    override val cardReader: CardReader = QPosCardReader()
    override val sessionData = PosManager.SessionData()

    override suspend fun loadEmv() {
        pos.setContext(activity)
        //init handler
    }

    override suspend fun startTransaction(): TransactionResponse {
        TODO("Not yet implemented")
    }

    override fun cleanUpEmv() {
        cardReader.endWatch()
        pos.stopScanQPos2Mode()
        listener.cleanup()
        if (pos == null) return
        pos.disconnectBT()
    }

    inner class QPosCardReader : CardReader {
        private var watchJob: Job? = null

        override suspend fun waitForCard(): CardReaderEvent {
            Looper.myLooper() ?: Looper.prepare()
            val handler = Handler(Looper.myLooper()!!)
            pos.initListener(handler, listener)
            pos.clearBluetoothBuffer()
            pos.setTerminalID(posConfig.terminalId, 5000)
            val connected: Boolean = connectDevice()
            if (!connected) return suspendCoroutine { continuation ->
                showError<Nothing>("Could not connect to device") {
                    onClose { continuation.resume(CardReaderEvent.CANCELLED) }
                }
            }
            updateEmvConfig()

            return CardReaderEvent.CHIP
        }

        override suspend fun read(amountStr: String): CardData? = suspendCoroutine { continuation ->
            listener.cardDataContinuation = continuation
            pos.doTrade(keyIndex, 60)
        }

        override fun endWatch() {
            watchJob?.cancel()
            watchJob = null
        }

        override suspend fun onRemoveCard(onEventChange: CardReaderEventListener) {
            watchJob = GlobalScope.launch {
                val connected = suspendCoroutine<Boolean> { continuation ->
                    listener.posConnectContinuation = continuation
                }
                watchJob = null
                if (!connected) onEventChange(CardReaderEvent.Timeout)
            }
        }
    }

    fun updateEmvConfig() {
        val emvAppCfg =
            QPOSUtil.byteArray2Hex(FileUtils.readAssetsLine("emv_app.bin", activity))
        val emvCapkCfg =
            QPOSUtil.byteArray2Hex(FileUtils.readAssetsLine("emv_capk.bin", activity))
        pos.updateEmvConfig(emvAppCfg, emvCapkCfg)
    }

    init {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
    }

    private suspend fun connectDevice(): Boolean {
        showProgressBar("Connecting to mPOS device")
        val connected: Boolean = withContext(Dispatchers.IO) {
//            listener.posConnectContinuation = continuation
            pos.syncConnectBluetooth(true, 25, "04:D1:6E:54:1E:02")
        }
        hideProgressBar()
        return connected
    }


    companion object : PosManagerCompanion {
        override val module = module {
            factory<PosManager>(override = true) { (activity: CreditClubActivity) ->
                QPosManager(activity)
            }
            factory<PosPrinter>(override = true) { (activity: CreditClubActivity) ->
                DummyPosPrinter(activity)
            }
        }

        override fun isCompatible(context: Context): Boolean {
            return try {
                BluetoothAdapter.getDefaultAdapter() != null
            } catch (ex: Exception) {
                false
            }
        }

        override fun setup(context: Context) {

        }
    }
}