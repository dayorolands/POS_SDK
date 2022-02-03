package com.cluster

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.cluster.databinding.DialogCustomerRequestOptionsBinding
import com.cluster.core.R
import com.cluster.core.data.api.StaticService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.model.AccountInfo
import com.cluster.core.data.request.BalanceEnquiryRequest
import com.cluster.core.data.request.ConfirmTokenRequest
import com.cluster.core.data.request.SendTokenRequest
import com.cluster.core.type.CustomerRequestOption
import com.cluster.core.type.TokenType
import com.cluster.core.ui.CreditClubActivity
import com.cluster.core.ui.widget.*
import com.cluster.core.util.*
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import java.security.SecureRandom
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 28/08/2019.
 * Appzone Ltd
 */

val tokenParams = TextFieldParams(
    "Enter Token",
    maxLength = 10,
    type = "number",
    helperText = "A token has been sent to the customer's phone number",
)

/***
 * Show this [Dialog] when [DialogListener.onClose] is called
 */
inline val Dialog.showOnClose: DialogListenerBlock<*>
    get() = {
        onClose {
            show()
        }
    }

fun CreditClubActivity.requireAccountInfo(
    title: String = "Get customer by...",
    options: List<CustomerRequestOption> = listOf(
        CustomerRequestOption.PhoneNumber,
        CustomerRequestOption.AccountNumber
    ),
    block: DialogListenerBlock<AccountInfo>,
) {
    mainScope.launch {
        val listener = block.build()
        val accountInfo = requireAccountInfo(title, options)
        if (accountInfo == null) listener.close()
        else listener.submit(Dialog(this@requireAccountInfo), accountInfo)
    }
}

suspend fun CreditClubActivity.requireAccountInfo(
    title: String = "Get customer by...",
    options: List<CustomerRequestOption> = listOf(
        CustomerRequestOption.PhoneNumber,
        CustomerRequestOption.AccountNumber,
    ),
): AccountInfo? {
    if (options.size == 1) {
        return suspendCoroutine {
            requestAccountInfo(options.first(), it)
        }
    } else {
        val option = showCustomerRequestOptions(title, options) ?: return null
        return suspendCoroutine {
            requestAccountInfo(option, it)
        }
    }
}

fun CreditClubActivity.requestAccountInfo(
    option: CustomerRequestOption,
    continuation: Continuation<AccountInfo?>,
) {
    val staticService: StaticService by retrofitService()
    val params = when (option) {
        CustomerRequestOption.PhoneNumber -> TextFieldParams(
            "Enter customer phone number",
            type = "number",
            maxLength = 11,
            minLength = 11,
            required = true,
        )

        CustomerRequestOption.AccountNumber -> TextFieldParams(
            "Enter customer account number",
            type = "number",
            maxLength = institutionConfig.bankAccountNumberLength,
            minLength = institutionConfig.bankAccountNumberLength,
            required = true,
        )
    }

    dialogProvider.showInput(params) {
        onSubmit { text ->
            mainScope.launch {
                val institutionCode = localStorage.institutionCode

                hide()

                when (option) {
                    CustomerRequestOption.PhoneNumber -> {
                        dialogProvider.showProgressBar("Getting customer accounts")

                        val (response, error) = safeRunIO {
                            staticService.getCustomerAccountByPhoneNumber(
                                institutionCode,
                                text
                            )
                        }

                        dialogProvider.hideProgressBar()

                        if (error != null && error.isKotlinNPE()) return@launch dialogProvider.showError(
                            "Phone Number is invalid",
                            showOnClose,
                        )
                        if (error != null) return@launch showError(
                            error,
                            showOnClose,
                        )

                        response ?: return@launch dialogProvider.showError(
                            "Phone Number is not registered",
                            showOnClose,
                        )

                        var linkingBankAccounts = response.linkingBankAccounts
                        if (linkingBankAccounts == null) {
                            dialogProvider.showError(
                                response.responseMessage
                                    ?: "Phone Number is not registered",
                                showOnClose,
                            )
                            return@launch
                        }
                        linkingBankAccounts = linkingBankAccounts.map {
                            it.copy(phoneNumber = response.phoneNumber)
                        }

                        dismiss()
                        val accountInfo = selectAccountNumber(linkingBankAccounts)
                        if (accountInfo == null) {
                            continuation.resume(null)
                            return@launch
                        }
                        continuation.resume(accountInfo)
                    }

                    CustomerRequestOption.AccountNumber -> {
                        dialogProvider.showProgressBar("Validating")
                        val (response, error) = safeRunIO {
                            staticService.getCustomerAccountByAccountNumber(
                                institutionCode,
                                text
                            )
                        }
                        dialogProvider.hideProgressBar()

                        if (error != null && (error is SerializationException || error.isKotlinNPE())) {
                            return@launch dialogProvider.showError(
                                "Account Number is invalid",
                                showOnClose,
                            )
                        }
                        if (error != null) return@launch showError(
                            error,
                            showOnClose,
                        )

                        response ?: return@launch dialogProvider.showError(
                            "Account Number is invalid",
                            showOnClose,
                        )

                        if (response.number.isEmpty()) return@launch dialogProvider.showError(
                            response.responseMessage ?: "Account number is invalid",
                            showOnClose,
                        )

                        dismiss()
                        continuation.resume(response)
                    }
                }
            }
        }

        onClose {
            continuation.resume(null)
        }
    }
}

suspend fun CreditClubActivity.sendToken(
    accountInfo: AccountInfo,
    operationType: TokenType,
    amount: Double = 1.0,
): Boolean {
    val staticService: StaticService by retrofitService()
    val reference = SecureRandom().nextInt(1000000)
    val sendTokenRequest = SendTokenRequest(
        customerPhoneNumber = accountInfo.phoneNumber,
        customerAccountNumber = accountInfo.number,
        agentPhoneNumber = localStorage.agentPhone,
        agentPin = "0000",
        institutionCode = localStorage.institutionCode,
        amount = amount,
        isPinChange = operationType == TokenType.PinChange,
        operationType = operationType.label,
        referenceNumber = if (operationType != TokenType.Withdrawal) "$reference" else null,
    )

    dialogProvider.showProgressBar("Sending Token")
    val (data, error) = safeRunIO {
        staticService.sendToken(sendTokenRequest)
    }
    dialogProvider.hideProgressBar()

    if (error != null) {
        dialogProvider.showErrorAndWait(error)
        return false
    }
    if (data == null) {
        dialogProvider.showErrorAndWait("A network-related error occurred while sending token")
        return false
    }

    if (!data.isSuccessful) {
        val errorMessage = data.responseMessage ?: "An error occurred while sending token"
        dialogProvider.showErrorAndWait(errorMessage)
        return false
    }

    dialogProvider.showSuccess(data.responseMessage)

    return true
}


inline fun CreditClubActivity.requireAndValidateToken(
    accountInfo: AccountInfo,
    amount: Double = 1.0,
    operationType: TokenType,
    isPinChange: Boolean = false,
    textFieldParams: TextFieldParams = tokenParams,
    crossinline block: DialogListenerBlock<Unit>,
) {
    val staticService: StaticService by retrofitService()
    val reference = SecureRandom().nextInt(1000000)

    val sendTokenRequest = SendTokenRequest(
        customerPhoneNumber = accountInfo.phoneNumber,
        customerAccountNumber = accountInfo.number,
        agentPhoneNumber = localStorage.agentPhone,
        agentPin = "0000",
        institutionCode = localStorage.institutionCode,
        amount = amount,
        referenceNumber = "$reference",
        isPinChange = isPinChange,
        operationType = operationType.label,
    )

    mainScope.launch {
        dialogProvider.showProgressBar("Sending Token")

        val (response, error) = safeRunIO {
            staticService.sendToken(sendTokenRequest)
        }

        dialogProvider.hideProgressBar()

        if (error != null) return@launch showError(error, stripType(block))
        response ?: return@launch showNetworkError(stripType(block))

        if (!response.isSuccessful) {
            dialogProvider.showError(
                response.responseMessage
                    ?: "An error occurred while sending token. Please try again later"
            )
            return@launch
        }

        dialogProvider.showInput(textFieldParams) {
            onSubmit { token ->
                dismiss()

                val confirmTokenRequest = ConfirmTokenRequest()
                confirmTokenRequest.customerPhoneNumber = accountInfo.phoneNumber
                confirmTokenRequest.customerAccountNumber = accountInfo.number
                confirmTokenRequest.agentPhoneNumber = localStorage.agentPhone
                confirmTokenRequest.agentPin = "0000"
                confirmTokenRequest.institutionCode = localStorage.institutionCode
                confirmTokenRequest.referenceNumber = "$reference"
                confirmTokenRequest.token = token

                mainScope.launch {
                    dialogProvider.showProgressBar("Confirming token")

                    val (confirmTokenResponse) = safeRunIO {
                        staticService.confirmToken(confirmTokenRequest)
                    }

                    dialogProvider.hideProgressBar()

                    if (confirmTokenResponse == null) {
                        dialogProvider.showError(
                            "A network-related error occurred while sending token",
                            stripType(block)
                        )
                        return@launch
                    }

                    if (confirmTokenResponse.isSuccessful) {
                        runOnUiThread {
                            DialogListener.create(block).submit(this@onSubmit, Unit)
                        }
                    } else {
                        dialogProvider.showError(
                            confirmTokenResponse.responseMessage
                                ?: "An error occurred. Please try again later",
                            stripType(block)
                        )
                    }
                }
            }
        }
    }
}

suspend fun CreditClubActivity.selectAccountNumber(linkingBankAccounts: List<AccountInfo>): AccountInfo? {
    when (linkingBankAccounts.size) {
        0 -> {
            dialogProvider.showErrorAndWait("This customer has no linked accounts")
            return null
        }
        1 -> return linkingBankAccounts.first()
        else -> {
            val accountOptions = linkingBankAccounts.map {
                DialogOptionItem(title = it.accountName, subtitle = it.number.mask(4, 2))
            }

            val selectedPosition =
                dialogProvider.getSelection("Select account number", accountOptions)
                    ?: return null
            return linkingBankAccounts[selectedPosition]
        }
    }
}

suspend fun CreditClubActivity.customerBalanceEnquiry(accountInfo: AccountInfo) {
    val staticService: StaticService by retrofitService()
    val pin = dialogProvider.getPin(getString(R.string.agent_pin)) ?: return
    if (pin.length != 4) {
        dialogProvider.showError(getString(R.string.agent_pin_must_be_4_digits))
        return
    }

    val request = BalanceEnquiryRequest(
        customerAccountNumber = accountInfo.number,
        agentPin = pin,
        agentPhoneNumber = localStorage.agentPhone,
        geoLocation = localStorage.lastKnownLocation,
        institutionCode = localStorage.institutionCode,
        deviceNumber = localStorage.deviceNumber,
    )

    dialogProvider.showProgressBar("Sending balance to customer")
    val (response, error) = safeRunIO {
        staticService.balanceEnquiry(request)
    }
    dialogProvider.hideProgressBar()

    if (error.isNetworkError()) {
        showNetworkError()
        return
    }

    if (response == null) {
        showNetworkError()
        return
    }

    if (response.isSussessful) {
        dialogProvider.showSuccessAndWait("${response.responseMessage}")
    } else {
        dialogProvider.showErrorAndWait(
            response.responseMessage ?: getString(R.string.an_error_occurred_please_try_again_later)
        )
    }
}

inline fun <T> stripType(crossinline block: DialogListenerBlock<T>): DialogListenerBlock<*> {
    return {
        onClose { DialogListener.create(block).close() }
    }
}

suspend fun CreditClubActivity.showCustomerRequestOptions(
    title: CharSequence,
    available: List<CustomerRequestOption>,
): CustomerRequestOption? = suspendCoroutine { continuation ->
    val dialog = Dialog(this).apply {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }

    val inflater = LayoutInflater.from(this)
    val binding = DataBindingUtil.inflate<DialogCustomerRequestOptionsBinding>(
        inflater,
        com.cluster.R.layout.dialog_customer_request_options,
        null,
        false,
    )
    binding.title = title

    if (available.contains(CustomerRequestOption.AccountNumber)) {
        binding.buttonAccountNumber.setOnClickListener {
            dialog.dismiss()
            continuation.resume(CustomerRequestOption.AccountNumber)
        }
    } else binding.buttonAccountNumber.visibility = View.GONE

    if (available.contains(CustomerRequestOption.PhoneNumber)) {
        binding.buttonPhoneNumber.setOnClickListener {
            dialog.dismiss()
            continuation.resume(CustomerRequestOption.PhoneNumber)
        }
    } else binding.buttonPhoneNumber.visibility = View.GONE

    dialog.setContentView(binding.root)
    binding.buttonCancel.setOnClickListener {
        dialog.dismiss()
        continuation.resume(null)
    }
    dialog.setOnCancelListener {
        dialog.dismiss()
        continuation.resume(null)
    }
    dialog.show()
}