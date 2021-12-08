package pos.providers.wizar

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.cloudpos.jniinterface.EMVJNIInterface
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.core.util.debug
import com.cluster.core.util.debugOnly
import com.cluster.core.util.safeRun
import com.cluster.pos.PosConfig
import com.cluster.pos.PosManager
import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosParameter
import com.cluster.pos.card.CardReader
import com.cluster.pos.printer.PosPrinter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.wizarpos.security.injectkey.aidl.IKeyLoaderService
import org.koin.dsl.module


open class WizarPosManager(
    private val activity: CreditClubActivity,
    private val posParameter: PosParameter,
    private val posConfig: PosConfig,
) : PosManager {
    override val cardReader: CardReader by lazy {
        WizarCardReader(
            activity = activity,
            sessionData = sessionData,
            defaultPosParameter = posParameter,
            posConfig = posConfig,
            terminalConfig = wizarTerminalConfig,
        )
    }
    private var service: IKeyLoaderService? = null
    private var authInfo = byteArrayOf()

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

        startInjectKeyService(activity)
    }

    override fun cleanUpEmv() {
        if (service != null) {
            service = null
            activity.unbindService(serviceConnection)
        }
        safeRun { EMVJNIInterface.close_reader(1) }
    }

    @Synchronized
    protected fun startConnectService(
        context: Context,
        comp: ComponentName,
        connection: ServiceConnection?,
    ): Boolean {
        val intent = Intent()
        intent.setPackage(comp.packageName)
        intent.component = comp
        val isSuccess = context.bindService(intent, connection!!, Context.BIND_AUTO_CREATE)
        debug("(%s)bind service (%s, %s) isSuccess=${isSuccess}, comp.packageName=${comp.packageName}, comp.className=${comp.className}")
        return isSuccess
    }

    private fun startInjectKeyService(context: Context): Boolean {
        val comp = ComponentName(
            "com.wizarpos.security.injectkey",
            "com.wizarpos.security.injectkey.service.MainService")
        return startConnectService(activity, comp, serviceConnection)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, obj: IBinder?) {
            service = IKeyLoaderService.Stub.asInterface(obj)
            service!!.resetMasterKey(0)
            authInfo = service!!.authInfo
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
        }
    }

    companion object : PosManagerCompanion {
        override val id = "WizarPOS"
        override val deviceType = 5

        override val module = module {
            factory<PosManager> { (activity: CreditClubActivity) ->
                WizarPosManager(
                    activity = activity,
                    posParameter = get(),
                    posConfig = get(),
                )
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
                FirebaseCrashlytics.getInstance().recordException(ex)
            } catch (err: UnsatisfiedLinkError) {
                debugOnly { Log.e("WizarPosManager", err.message, err) }
                FirebaseCrashlytics.getInstance().recordException(err)
            }

            return false
        }

        override fun setup(context: Context) {

        }
    }
}
