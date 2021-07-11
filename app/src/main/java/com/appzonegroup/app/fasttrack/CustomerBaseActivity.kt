package com.appzonegroup.app.fasttrack

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import com.appzonegroup.app.fasttrack.ui.TransactionSummary
import com.creditclub.core.data.model.AccountInfo
import com.creditclub.core.data.model.AgentFee
import com.creditclub.core.data.response.GenericResponse
import com.creditclub.core.type.CustomerRequestOption
import kotlinx.coroutines.CoroutineScope
import java.util.*


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 29/08/2019.
 * Appzone Ltd
 */

@SuppressLint("Registered")
abstract class CustomerBaseActivity(protected var flowName: String? = null) : BaseActivity() {
    protected var accountInfo = AccountInfo()
    protected var flowId: String? = UUID.randomUUID().toString().substring(0, 8)

    inline val customerRequestOptions: List<CustomerRequestOption>
        get() = resources.getStringArray(R.array.customer_request_options).map { value ->
            val index = value.split(',').first().toInt() - 1
            CustomerRequestOption.values()[index]
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireAccountInfo(options = customerRequestOptions) {
            onSubmit {
                accountInfo = it
                onCustomerReady(savedInstanceState)
            }

            onClose {
                finish()
            }
        }
    }

    abstract fun onCustomerReady(savedInstanceState: Bundle?)

    fun renderTransactionSummary(
        amount: Double,
        onProceed: suspend CoroutineScope.() -> Unit,
        fetchFeeAgent: suspend CoroutineScope.() -> GenericResponse<AgentFee>?,
    ) {
        setContent {
            TransactionSummary(
                amount = amount,
                onProceed = onProceed,
                fetchFeeAgent = fetchFeeAgent,
                accountInfo = accountInfo,
            )
        }
    }
}
