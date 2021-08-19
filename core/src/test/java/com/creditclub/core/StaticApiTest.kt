package com.creditclub.core

import com.creditclub.core.data.api.CaseLogService
import com.creditclub.core.data.api.StaticService
import com.creditclub.core.data.request.CaseDetailsRequest
import com.creditclub.core.util.delegates.service
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
            val caseLogService by middleWareAPI.retrofit.service<CaseLogService>()
            val (response, error) = safeRunIO {
                caseLogService.caseDetails(
                    CaseDetailsRequest(
                        agentPhoneNumber = "08182709167",
                        institutionCode = "100287",
                    )
                )
            }

            assert(error == null)
            assert(response != null)
        }
    }

    @Test
    fun `all products are fetched successfully`() {
        val staticService by middleWareAPI.retrofit.service<StaticService>()
        mainScope {
            val (response, error) = safeRunIO {
                staticService.getAllProducts("100287", "08182709167")
            }

            assert(response != null)
        }
    }
}