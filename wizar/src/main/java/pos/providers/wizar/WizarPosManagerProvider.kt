package pos.providers.wizar

import com.creditclub.pos.PosManagerCompanion
import com.creditclub.pos.PosManagerProvider

class WizarPosManagerProvider : PosManagerProvider(BuildConfig.LIBRARY_PACKAGE_NAME, true) {
    override val posManagerCompanion: PosManagerCompanion
        get() = WizarPosManager
}