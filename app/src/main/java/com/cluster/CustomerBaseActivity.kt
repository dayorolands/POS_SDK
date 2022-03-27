package com.cluster

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cluster.core.data.model.AccountInfo
import com.cluster.core.data.model.AgentFee
import com.cluster.core.data.response.GenericResponse
import com.cluster.core.type.CustomerRequestOption
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.util.SuspendCallback
import com.cluster.pos.printer.PrintJob
import com.cluster.screen.ReceiptDetails
import com.cluster.ui.TransactionSummary
import com.cluster.ui.theme.CreditClubTheme
import com.google.accompanist.insets.ProvideWindowInsets
import java.util.*


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 29/08/2019.
 * Appzone Ltd
 */

@SuppressLint("Registered")
abstract class CustomerBaseActivity(protected var flowName: String? = null) : CreditClubActivity() {
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

    protected fun renderTransactionSummary(
        amount: Double,
        onProceed: SuspendCallback,
        fetchFeeAgent: suspend () -> GenericResponse<AgentFee>?,
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

    protected fun renderReceiptDetails(receipt: PrintJob) {
        setContent {
            val navController = rememberNavController()
            CreditClubTheme {
                ProvideWindowInsets {
                    NavHost(navController = navController, startDestination = Routes.Receipt) {
                        composable(Routes.Receipt) {
                            ReceiptDetails(
                                navController = navController,
                                onBackPressed = { finish() },
                                printJob = receipt,
                                showAppBar = false,
                            )
                        }
                    }
                }
            }
        }
    }

    fun addValidPhoneNumberListener(editText: EditText) {

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                phoneNumberEditTextFilter(editText, this)
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    fun phoneNumberEditTextFilter(editText: EditText, textWatcher: TextWatcher) {
        val numbers = "0123456789"

        val text = editText.text.toString().trim { it <= ' ' }

        val textToCharArray = text.toCharArray()

        val accumulator = StringBuilder()

        for (c in textToCharArray) {
            if (numbers.contains(c + "")) {
                accumulator.append(c)
            }
        }
        editText.removeTextChangedListener(textWatcher)

        //This line without the line before and after will cause endless loop
        //of call to the text changed listener
        editText.setText(accumulator)
        editText.addTextChangedListener(textWatcher)
        editText.setSelection(accumulator.length)
    }

    fun goBack(v: View) {
        onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showError(message: String?) = dialogProvider.showError(message)

    fun showSuccess(message: String?) = dialogProvider.showSuccess(message)

    open fun indicateError(message: String?, view: EditText?) =
        dialogProvider.indicateError(message, view)

    fun showProgressBar(title: String) = dialogProvider.showProgressBar(title)

    fun hideProgressBar() = dialogProvider.hideProgressBar()
}
