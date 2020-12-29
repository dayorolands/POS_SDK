package com.appzonegroup.app.fasttrack

import android.annotation.SuppressLint
import android.os.Bundle
import com.creditclub.core.data.model.AccountInfo
import com.creditclub.core.type.CustomerRequestOption
import com.creditclub.core.util.requireAccountInfo
import java.util.*


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 29/08/2019.
 * Appzone Ltd
 */

@SuppressLint("Registered")
abstract class CustomerBaseActivity(protected var flowName: String? = null) : BaseActivity() {
    protected var accountInfo = AccountInfo()
    protected var flowId: String? = UUID.randomUUID().toString().substring(0, 8)

    inline val customerRequestOptions: Array<CustomerRequestOption>
        get() = resources.getStringArray(R.array.customer_request_options).map {
            val indexAndLabel = it.split(",")
            CustomerRequestOption.values()[indexAndLabel.first().toInt()]
        }.toTypedArray()

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
}