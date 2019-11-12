package com.appzonegroup.app.fasttrack

import android.annotation.SuppressLint
import android.os.Bundle
import com.creditclub.core.data.model.AccountInfo
import com.creditclub.core.type.CustomerRequestOption
import com.creditclub.core.ui.widget.DialogListenerBlock
import com.creditclub.core.util.requireAccountInfo


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 29/08/2019.
 * Appzone Ltd
 */

@SuppressLint("Registered")
abstract class CustomerBaseActivity : BaseActivity() {
    protected var accountInfo = AccountInfo()

    val customerRequestOptions: Array<CustomerRequestOption>
        get() = resources.getStringArray(R.array.customer_request_options).map {
            val indexAndLabel = it.split(",")
            CustomerRequestOption.values()[indexAndLabel.first().toInt()]
        }.toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireAccountInfo(available = customerRequestOptions) {
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

    @JvmOverloads
    fun javaRequireAccountInfo(
        title: String = "Get customer by...",
        available: Array<CustomerRequestOption> = customerRequestOptions,
        block: DialogListenerBlock<AccountInfo>
    ) = requireAccountInfo(title, available, block)
}