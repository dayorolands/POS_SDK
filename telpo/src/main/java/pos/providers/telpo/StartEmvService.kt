package pos.providers.telpo

import android.content.Context
import com.telpo.emv.EmvService


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */
class StartEmvService(val context: Context) : Runnable {
    override fun run() {
        var ret = EmvService.Open(context)
        if (ret != EmvService.EMV_TRUE) {
            return
        }

        ret = EmvService.deviceOpen()
        if (ret != 0) {
            return
        }

        EmvService.NfcOpenReader(1000)
    }
}