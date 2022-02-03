package pos.providers.telpo

import com.cluster.pos.PosManagerCompanion
import com.cluster.pos.PosManagerProvider

class TelpoPosManagerProvider : PosManagerProvider(packageName = BuildConfig.LIBRARY_PACKAGE_NAME) {
    override val posManagerCompanion: PosManagerCompanion
        get() = TelpoPosManager
}