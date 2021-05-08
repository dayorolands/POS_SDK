package com.creditclub.core.util

import android.app.Dialog
import com.creditclub.core.R
import com.creditclub.core.data.model.AccountInfo
import com.creditclub.core.data.request.BalanceEnquiryRequest
import com.creditclub.core.data.request.ConfirmTokenRequest
import com.creditclub.core.data.request.SendTokenRequest
import com.creditclub.core.type.CustomerRequestOption
import com.creditclub.core.type.TokenType
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.*
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import java.security.SecureRandom


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 28/08/2019.
 * Appzone Ltd
 */

private val tokenParams = TextFieldParams(
    "Enter Token",
    maxLength = 10,
    type = "number",
    helperText = "A token has been sent to the customer's phone number"
)

fun CreditClubActivity.requireAccountInfo(
    title: String = "Get customer by...",
    options: Array<CustomerRequestOption> = arrayOf(
        CustomerRequestOption.PhoneNumber,
        CustomerRequestOption.AccountNumber
    ),
    block: DialogListenerBlock<AccountInfo>
) {
    val listener = DialogListener.create(block)

    val requestDialogBlock: DialogListenerBlock<CustomerRequestOption> = {
        onSubmit { option ->
            dismiss()

            val params = when (option) {
                CustomerRequestOption.BVN -> TextFieldParams(
                    "Enter customer BVN",
                    type = "number",
                    maxLength = institutionConfig.bankAccountNumberLength,
                    required = true,
                )

                CustomerRequestOption.PhoneNumber -> TextFieldParams(
                    "Enter customer phone number",
                    type = "number",
                    maxLength = institutionConfig.bankAccountNumberLength,
                    minLength = institutionConfig.bankAccountNumberLength,
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
                        val staticService = creditClubMiddleWareAPI.staticService
                        val institutionCode = localStorage.institutionCode

                        hide()

                        when (option) {
                            CustomerRequestOption.BVN -> {
                                dismiss()
                                dialogProvider.showError(
                                    "Feature unavailable for this version",
                                    stripType(block)
                                )
                            }

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
                                    stripType(showOnClose)
                                )
                                if (error != null) return@launch showError(
                                    error,
                                    stripType(showOnClose)
                                )

                                response ?: return@launch dialogProvider.showError(
                                    "Phone Number is not registered",
                                    stripType(showOnClose)
                                )

                                response.linkingBankAccounts
                                    ?: return@launch dialogProvider.showError(
                                        response.responseMessage
                                            ?: "Phone Number is not registered",
                                        stripType(showOnClose)
                                    )

                                response.linkingBankAccounts?.run {
                                    forEach {
                                        it.phoneNumber = response.phoneNumber
                                    }

                                    dismiss()
                                    selectAccountNumber(this) {
                                        onSubmit { accountInfo ->
                                            listener.submit(this@onSubmit, accountInfo)
                                        }

                                        onClose {
                                            listener.close()
                                        }
                                    }
                                }
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
                                        stripType(showOnClose)
                                    )
                                }
                                if (error != null) return@launch showError(
                                    error,
                                    stripType(showOnClose)
                                )

                                response ?: return@launch dialogProvider.showError(
                                    "Account Number is invalid",
                                    stripType(showOnClose)
                                )

                                if (response.number.isEmpty()) return@launch dialogProvider.showError(
                                    response.responseMessage ?: "Account number is invalid",
                                    stripType(showOnClose)
                                )

                                dismiss()
                                listener.submit(this@onSubmit, response)
                            }
                        }
                    }
                }

                onClose {
                    listener.close()
                }
            }
        }

        onClose {
            listener.close()
        }
    }

    if (options.size == 1) {
        DialogListener.create(requestDialogBlock).submit(Dialog(this), options.first())
    } else {
        dialogProvider.showCustomerRequestOptions(title, options, requestDialogBlock)
    }
}

suspend fun CreditClubActivity.sendToken(
    accountInfo: AccountInfo,
    operationType: TokenType,
    amount: Double = 1.0,
): Boolean {
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
    )

    if (operationType != TokenType.Withdrawal) {
        sendTokenRequest.referenceNumber = "$reference"
    }

    dialogProvider.showProgressBar("Sending Token")
    val (data, error) = safeRunIO {
        creditClubMiddleWareAPI.staticService.sendToken(sendTokenRequest)
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


fun CreditClubActivity.requireAndValidateToken(
    accountInfo: AccountInfo,
    amount: Double = 1.0,
    operationType: TokenType,
    isPinChange: Boolean = false,
    textFieldParams: TextFieldParams = tokenParams,
    block: DialogListenerBlock<Unit>
) {
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
            creditClubMiddleWareAPI.staticService.sendToken(sendTokenRequest)
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
                        creditClubMiddleWareAPI.staticService.confirmToken(confirmTokenRequest)
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

fun CreditClubActivity.selectAccountNumber(
    accountInfo: ArrayList<AccountInfo>,
    block: DialogListenerBlock<AccountInfo>
) {

    val listener = DialogListener.create(block)

    when (accountInfo.size) {

        0 -> dialogProvider.showError("This customer has no linked accounts", stripType(block))

        1 -> listener.submit(Dialog(this), accountInfo.first())

        else -> {

            val accountOptions = accountInfo.map {
                DialogOptionItem(title = it.accountName, subtitle = it.number.mask(4, 2))
            }

            dialogProvider.showOptions("Select account number", accountOptions) {
                onSubmit { selectedPosition ->
                    listener.submit(this, accountInfo[selectedPosition])
                }

                onClose {
                    listener.close()
                }
            }
        }
    }
}

suspend fun CreditClubActivity.customerBalanceEnquiry(accountInfo: AccountInfo) {
    val pin = dialogProvider.getPin(getString(R.string.agent_pin)) ?: return
    if (pin.length != 4) {
        dialogProvider.showError(getString(R.string.agent_pin_must_be_4_digits))
        return
    }

    val request = BalanceEnquiryRequest(
        customerAccountNumber = accountInfo.number,
        agentPin = pin,
        agentPhoneNumber = localStorage.agentPhone,
        geoLocation = gps.geolocationString,
        institutionCode = localStorage.institutionCode,
    )

    dialogProvider.showProgressBar("Sending balance to customer")
    val (response, error) = safeRunIO {
        creditClubMiddleWareAPI.staticService.balanceEnquiry(request)
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