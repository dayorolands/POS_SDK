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
import com.creditclub.core.ui.widget.DialogListener
import com.creditclub.core.ui.widget.DialogListenerBlock
import com.creditclub.core.ui.widget.DialogOptionItem
import com.creditclub.core.ui.widget.TextFieldParams
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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
                    maxLength = 11
                )

                CustomerRequestOption.PhoneNumber -> TextFieldParams(
                    "Enter customer phone number",
                    type = "number",
                    maxLength = 11
                )

                CustomerRequestOption.AccountNumber -> TextFieldParams(
                    "Enter customer account number",
                    type = "number",
                    maxLength = 11
                )
            }

            dialogProvider.showInput(params) {
                onSubmit { text ->
                    GlobalScope.launch(Dispatchers.Main) {
                        val staticService = creditClubMiddleWareAPI.staticService
                        val institutionCode = localStorage.institutionCode

                        hide()

                        when (option) {
                            CustomerRequestOption.BVN -> {
                                dismiss()
                                dialogProvider.showError(
                                    "Feature unavailable for this version",
                                    block
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
                                    showOnClose
                                )
                                if (error != null) return@launch showError(error, showOnClose)

                                response ?: return@launch dialogProvider.showError(
                                    "Phone Number is not registered",
                                    showOnClose
                                )

                                response.linkingBankAccounts
                                    ?: return@launch dialogProvider.showError(
                                        response.responseMessage
                                            ?: "Phone Number is not registered",
                                        showOnClose
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
                                        showOnClose
                                    )
                                }
                                if (error != null) return@launch showError(error, showOnClose)

                                response ?: return@launch dialogProvider.showError(
                                    "Account Number is invalid",
                                    showOnClose
                                )

                                if (response.number.isEmpty()) return@launch dialogProvider.showError(
                                    response.responseMessage ?: "Account number is invalid",
                                    showOnClose
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

fun CreditClubActivity.sendToken(
    accountInfo: AccountInfo,
    operationType: TokenType,
    amount: Double = 1.0,
    block: DialogListenerBlock<Unit>
) {
    val reference = SecureRandom().nextInt(1000000)

    val sendTokenRequest = SendTokenRequest()
    sendTokenRequest.customerPhoneNumber = accountInfo.phoneNumber
    sendTokenRequest.customerAccountNumber = accountInfo.number
    sendTokenRequest.agentPhoneNumber = localStorage.agentPhone
    sendTokenRequest.agentPin = "0000"
    sendTokenRequest.institutionCode = localStorage.institutionCode
    sendTokenRequest.amount = amount

    if (operationType != TokenType.Withdrawal) {
        sendTokenRequest.referenceNumber = "$reference"
    }

    sendTokenRequest.isPinChange = operationType == TokenType.PinChange
    sendTokenRequest.operationType = operationType.label

    mainScope.launch {
        dialogProvider.showProgressBar("Sending Token")
        val (data, error) = safeRunIO {
            creditClubMiddleWareAPI.staticService.sendToken(sendTokenRequest)
        }
        dialogProvider.hideProgressBar()

        if (error != null) return@launch showError(error, block)
        if (data == null) {
            dialogProvider.showError("A network-related error occurred while sending token", block)
            return@launch
        }

        if (!data.isSuccessful) {
            val errorMessage =
                data.responseMessage ?: "An error occurred while sending token"

            dialogProvider.showError(errorMessage, block)

            return@launch
        }

        dialogProvider.showSuccess(data.responseMessage)

        val listener = DialogListener.create(block)
        listener.submit(Dialog(this@sendToken), Unit)
    }
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

    val sendTokenRequest = SendTokenRequest()
    sendTokenRequest.customerPhoneNumber = accountInfo.phoneNumber
    sendTokenRequest.customerAccountNumber = accountInfo.number
    sendTokenRequest.agentPhoneNumber = localStorage.agentPhone
    sendTokenRequest.agentPin = "0000"
    sendTokenRequest.institutionCode = localStorage.institutionCode
    sendTokenRequest.amount = amount
    sendTokenRequest.referenceNumber = "$reference"
    sendTokenRequest.isPinChange = isPinChange
    sendTokenRequest.operationType = operationType.label

    mainScope.launch {
        dialogProvider.showProgressBar("Sending Token")

        val (response, error) = safeRunIO {
            creditClubMiddleWareAPI.staticService.sendToken(sendTokenRequest)
        }

        dialogProvider.hideProgressBar()

        if (error != null) return@launch showError(error, block)
        response ?: return@launch showNetworkError(block)

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
                            block
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
                            block
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

        0 -> dialogProvider.showError("This customer has no linked accounts", block)

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

fun CreditClubActivity.customerBalanceEnquiry(accountInfo: AccountInfo) {
    dialogProvider.requestPIN("Enter agent PIN") {
        onSubmit { pin ->
            hide()

            if (pin.length != 4) {
                dialogProvider.showError("Agent PIN must be 4 digits", showOnClose)
                return@onSubmit
            }

            mainScope.launch {
                dialogProvider.showProgressBar("Sending balance to customer")

                val request = BalanceEnquiryRequest()
                request.customerAccountNumber = accountInfo.number
                request.agentPin = pin
                request.agentPhoneNumber = localStorage.agentPhone
                request.geoLocation = gps.geolocationString
                request.institutionCode = localStorage.institutionCode

                val staticService = creditClubMiddleWareAPI.staticService

                val (response, error) = safeRunIO {
                    staticService.balanceEnquiry(request)
                }

                dialogProvider.hideProgressBar()

                if (error.isNetworkError()) {
                    showNetworkError(showOnClose)
                    return@launch
                }

                response ?: return@launch showNetworkError()

                if (response.isSussessful) {
                    dismiss()
                    dialogProvider.showSuccess("${response.responseMessage}")
                } else {
                    dialogProvider.showError(
                        response.responseMessage
                            ?: getString(R.string.an_error_occurred_please_try_again_later),
                        showOnClose
                    )
                }
            }
        }
    }
}
