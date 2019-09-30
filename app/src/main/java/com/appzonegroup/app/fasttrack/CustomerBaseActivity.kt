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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireAccountInfo(available = arrayOf(CustomerRequestOption.AccountNumber)) {
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
        available: Array<CustomerRequestOption> = arrayOf(CustomerRequestOption.AccountNumber),
        block: DialogListenerBlock<AccountInfo>
    ) = requireAccountInfo(title, available, block)
}