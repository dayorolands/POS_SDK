package com.creditclub.core

import com.creditclub.core.data.request.CaseDetailsRequest
import com.creditclub.core.util.safeRunIO
import org.junit.Test


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 02/09/2019.
 * Appzone Ltd
 */
open class StaticApiTest : UnitTest() {

    @Test
    fun `all case details are fetched successfully`() {
        mainScope {
            val (response, error) = safeRunIO {
                middleWareAPI.caseLogService.caseDetails(CaseDetailsRequest(
                    agentPhoneNumber = "08182709167",
                    institutionCode = "100287",
                ))
            }

            assert(error == null)
            assert(response != null)
        }
    }

    @Test
    fun `all products are fetched successfully`() {
        mainScope {
            val (response, error) = safeRunIO {
                middleWareAPI.staticService.getAllProducts("100287", "08182709167")
            }

            assert(response != null)
        }
    }
}