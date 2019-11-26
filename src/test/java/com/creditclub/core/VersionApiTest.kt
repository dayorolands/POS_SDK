package com.creditclub.core

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
            val currentVersion = "0.0.1"

            val (response, error) = safeRunIO {
                middleWareAPI.versionService.getLatestVersionAndDownloadLink("CreditClub")
            }

            assert(error == null)
            assert(response != null)
            assert(currentVersion < response?.version ?: "0.0.0")
        }
    }
}