package com.creditclub.core

import com.creditclub.core.data.api.VersionService
import com.creditclub.core.data.model.SemVer
import com.creditclub.core.util.delegates.service
import com.creditclub.core.util.safeRunIO
import org.junit.Test


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 02/09/2019.
 * Appzone Ltd
 */
open class VersionApiTest : UnitTest() {
    val versionService: VersionService by middleWareAPI.retrofit.service()

    @Test
    fun `fetches app latest version`() {
        mainScope {
            val (response, error) = safeRunIO {
                versionService.getLatestVersionAndDownloadLink("AccessPOS")
            }

            assert(error == null)
            assert(response != null)
            SemVer.parse(response!!.version)
        }
    }

    @Test
    fun `version comparison works`() {
        assert(SemVer.parse("0") == SemVer.parse("0.0.0"))
        assert(SemVer.parse("1.4.10") > SemVer.parse("1.4.9"))
        assert(SemVer.parse("1.4.9") > SemVer.parse("1.4.8"))
        assert(SemVer.parse("1.40.86") > SemVer.parse("1.40.9"))
        assert(SemVer.parse("2.4.9") > SemVer.parse("1.40.86"))
        assert(SemVer.parse("2.4.9") > SemVer.parse("2.4.9-beta1"))
        assert(SemVer.parse("2.4.9-alpha2") > SemVer.parse("2.4.9-alpha1"))
    }
}