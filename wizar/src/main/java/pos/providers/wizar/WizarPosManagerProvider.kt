package pos.providers.wizar

import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosManagerProvider

class WizarPosManagerProvider : PosManagerProvider(BuildConfig.LIBRARY_PACKAGE_NAME, true) {
    override val posManagerCompanion: PosManagerCompanion
        get() = WizarPosManager
}