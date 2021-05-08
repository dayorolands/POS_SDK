package com.creditclub.core

import com.creditclub.core.data.model.Version
import com.creditclub.core.util.safeRunIO
import org.junit.Test


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 02/09/2019.
 * Appzone Ltd
 */
open class VersionApiTest : UnitTest() {

    @Test
    fun `fetches app latest version`() {
        mainScope {
            val (response, error) = safeRunIO {
                middleWareAPI.versionService.getLatestVersionAndDownloadLink("CreditClub")
            }

            assert(error == null)
            assert(response != null)
            Version(response!!.version)
        }
    }

    @Test
    fun `version comparison works`() {
        assert(Version("0") == Version("0.0.0"))
        assert(Version("1.4.9") < Version("1.4.10"))
        assert(Version("1.4.9") > Version("1.4.8"))
        assert(Version("1.40.9") < Version("1.40.86"))
        assert(Version("2.4.9") > Version("1.40.86"))
    }
}