package pos.providers.wizar

import android.content.Context
import android.util.Log
import com.cloudpos.jniinterface.EMVJNIInterface
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.debugOnly
import com.creditclub.core.util.safeRun
import com.creditclub.pos.PosConfig
import com.creditclub.pos.PosManager
import com.creditclub.pos.PosManagerCompanion
import com.creditclub.pos.PosParameter
import com.creditclub.pos.card.CardReader
import com.creditclub.pos.printer.PosPrinter
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module


class WizarPosManager(private val activity: CreditClubActivity) : PosManager, KoinComponent {
    private val posParameter: PosParameter by inject()
    private val posConfig: PosConfig by inject()
    override val cardReader: CardReader by lazy {
        WizarCardReader(
            activity = activity,
            sessionData = sessionData,
            defaultPosParameter = posParameter,
            posConfig = posConfig,
            terminalConfig = wizarTerminalConfig,
        )
    }

    private var emvParamLoadFlag: Boolean = false
    private val wizarTerminalConfig = WizarTerminalConfig(activity)
    override val sessionData = PosManager.SessionData()

    override suspend fun loadEmv() {
        if (emvParamLoadFlag) return
        //lib path
        var tmpEmvLibDir = activity.application.getDir("", 0).absolutePath
        tmpEmvLibDir =
            tmpEmvLibDir.substring(0, tmpEmvLibDir.lastIndexOf('/')) + "/lib/libEMVKernal.so"

        if (EMVJNIInterface.loadEMVKernel(tmpEmvLibDir.toByteArray(),
                tmpEmvLibDir.toByteArray().size).toInt() == 0
        ) {
            emvParamLoadFlag = true
        }
    }

    override fun cleanUpEmv() {
        safeRun { EMVJNIInterface.close_reader(1) }
    }

    companion object : PosManagerCompanion {
        override val id = "WizarPOS"
        override val deviceType = 5

        override val module = module {
            factory<PosManager> { (activity: CreditClubActivity) ->
                WizarPosManager(activity)
            }
            factory<PosPrinter> { (context: Context, dialogProvider: DialogProvider) ->
                WizarPrinter(
                    context,
                    dialogProvider
                )
            }
        }

        override fun isCompatible(context: Context): Boolean {
            try {
                val smartCardReaderDevice = getSmartCardDevice(context)
                smartCardReaderDevice.open()
                smartCardReaderDevice.close()
                return true
            } catch (ex: Exception) {
                debugOnly { Log.e("WizarPosManager", ex.message, ex) }
            } catch (err: UnsatisfiedLinkError) {
                debugOnly { Log.e("WizarPosManager", err.message, err) }
            }

            return false
        }

        override fun setup(context: Context) {

        }
    }
}
