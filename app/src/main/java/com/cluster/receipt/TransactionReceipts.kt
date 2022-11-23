@file:JvmMultifileClass
@file:JvmName("TransactionReceipts")

package com.cluster.receipt

import android.content.Context
import com.cluster.BuildConfig
import com.cluster.R
import com.cluster.core.data.model.*
import com.cluster.pos.printer.logo
import com.cluster.pos.receipt.transactionStatus
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.data.request.*
import com.cluster.core.data.response.CollectionPaymentResponse
import com.cluster.core.util.*
import com.cluster.core.util.delegates.defaultJson
import com.cluster.pos.printer.*
import com.cluster.pos.receipt.crossBankTransactionStatus
import com.cluster.pos.receipt.withdrawalTransactionStatus
import java.time.Instant

fun depositReceipt(
    context: Context,
    request: DepositRequest,
    accountInfo: AccountInfo,
    transactionDate: String,
    isSuccessful: Boolean = false,
    reason: String? = null,
    responseCode: String? = null
) = printJob {
    logo()
    text(
        text = "Deposit",
        align = Alignment.MIDDLE,
        fontSize = 35,
    )
    text(
        """
        |Agent Code: ${context.localStorage.agent?.agentCode}
        |Agent Phone: ${request.agentPhoneNumber}
        |--------------------------
        |Amount ${request.amount.toDouble().toCurrencyFormat()}
        |
        |Customer Account: ${accountInfo.number.mask(4, 2)}
        |Customer Name: ${accountInfo.accountName}
        |
        |Transaction Date: $transactionDate
        |RRN: ${request.deviceNumber}${request.retrievalReferenceNumber}""".trimMargin()
    )
    transactionStatus(
        context = context,
        isSuccessful = isSuccessful,
        responseCode = responseCode ?: if (isSuccessful) "00" else "06",
        reason = reason,
    )
    footer(context)
}

fun withdrawalReceipt(
    context: Context,
    request: WithdrawalRequest,
    accountInfo: AccountInfo,
    transactionDate: String,
    isSuccessful: Boolean = false,
    reason: String? = null,
) = printJob {
    logo()
    text(
        "Withdrawal",
        align = Alignment.MIDDLE,
        fontSize = 35,
    )
    text(
        """
        |Agent Code: ${context.localStorage.agent?.agentCode}
        |Agent Phone: ${request.agentPhoneNumber}
        |--------------------------
        |Amount : NGN${request.amount}
        |
        |Customer Account: ${accountInfo.number.mask(4, 2)}
        |Customer Name: ${accountInfo.accountName}
        |
        |Transaction Date: $transactionDate
        |RRN: ${request.deviceNumber}${request.retrievalReferenceNumber}""".trimMargin()
    )
    withdrawalTransactionStatus(
        context = context,
        isSuccessful = isSuccessful,
        reason = reason,
    )
    footer(context)
}

fun crossBankTokenReceipt(
    context: Context,
    request: CrossBankRequest,
    transactionDate: String,
    isSuccessful: Boolean = false,
    reason: String? = null,
    customerName: String?
) = printJob {
    logo()
    text(
        "Token Withdrawal",
        align = Alignment.MIDDLE,
        fontSize = 35,
    )
    text(
        """
        |Agent Code: ${context.localStorage.agent?.agentCode}
        |Agent Phone: ${request.agentPhoneNumber}
        |--------------------------
        |Amount : NGN${request.amount}
        |
        |Customer Account: ${request.customerAccountNumber.mask(4, 2)}
        |Customer Name: $customerName
        |
        |Transaction Date: $transactionDate
        |RRN: ${request.deviceNumber}${request.retrievalReferenceNumber}""".trimMargin()
    )
    crossBankTransactionStatus(
        context = context,
        isSuccessful = isSuccessful,
        reason = reason,
    )
    footer(context)
}

fun tokenWithdrawalReceipt(
    context: Context,
    request: SubmitTokenRequest,
    response: SubmitTokenResponse? = null,
    transactionDate: String,
    customerName: String?,
    bankName : String?
) = printJob {
    logo()
    text(
        text = "Token Withdrawal",
        align = Alignment.MIDDLE,
        fontSize = 35
    )
    text(
        """
        |Agent Code: ${context.localStorage.agent?.agentCode}
        |Agent Phone: ${context.localStorage.agentPhone}
        |--------------------------
        |Amount: NGN${request.amount}
        |
        |Bank: $bankName
        |Account Number: ${request.customerAccountNumber.mask(4,2)}
        |Customer Name: $customerName
        |RRN: ${request.deviceNumber}${request.retrievalReferenceNumber}
        |Transaction Date: $transactionDate
        """.trimMargin()
    )
    transactionStatus(
        context = context,
        isSuccessful = response?.isSuccessful == true,
        responseCode = response?.respCode ?: "06",
        reason = response?.message,
    )
    footer(context)
}

fun collectionPaymentReceipt(
    context: Context,
    request: CollectionPaymentRequest,
    transactionDate: String,
    response: CollectionPaymentResponse?
) = printJob {
    logo()
    text(
        text = "Collections",
        align = Alignment.MIDDLE,
        fontSize = 35,
    )
    text(
        """
        |Agent Code: ${context.localStorage.agent?.agentCode}
        |Agent Phone: ${context.localStorage.agentPhone}
        |--------------------------
        |Amount NGN${request.amount}
        |
        |Payment Item Name: ${request.billerItemName}
        |Payment Item Code: ${request.billerItemCode}
        |Biller Name: ${request.billerName}
        |Biller Code: ${request.billerCode}
        |Category: ${response?.collectionCategoryName}
        |Customer Phone number: ${request.customerPhoneNumber}
        |Customer Name: ${request.customerName}
        |RRN: ${request.deviceNumber}${request.retrievalReferenceNumber}
        |Reference: ${response?.collectionReference}
        |Transaction Date: $transactionDate
        """.trimMargin()
    )
    transactionStatus(
        context = context,
        isSuccessful = response?.isSuccessful == true,
        responseCode = response?.responseCode ?: "06",
        reason = response?.responseMessage,
    )
    footer(context)
}

fun collectionReportReceipt(
    context: Context,
    response: CollectionPaymentResponse?,
    request: CollectionReportRequest
) = printJob {
    logo()
    text(
        text = "Collections",
        align = Alignment.MIDDLE,
        fontSize = 35,
    )
    text(
        """
        |Agent Code: ${context.localStorage.agent?.agentCode}
        |Agent Phone: ${context.localStorage.agentPhone}
        |--------------------------
        |Amount NGN${response?.amount}
        |
        |Category: ${response?.collectionCategoryName}
        |Payment Item Name: ${response?.collectionPaymentItemName}
        |Customer Name: ${request.customerName}
        |RRN: ${request.deviceNumber}${request.retrievalReferenceNumber}
        |Reference: ${response?.collectionReference}
        |Transaction Date: ${response?.date?.toString("dd-MM-yyyy hh:mm:ss")?.replace("T"," ")}
        """.trimMargin()
    )
    transactionStatus(
        context = context,
        isSuccessful = response?.isSuccessful == true,
        responseCode = response?.responseCode ?: "06",
        reason = response?.responseMessage,
    )
    footer(context)
}

fun billsPaymentReceipt(
    context: Context,
    transactionDate: String,
    request: PayBillRequest,
    response: PayBillResponse? = null,
) = printJob {
    logo()
    text(
        text = if (request.isRecharge) "Airtime Recharge" else "Bills Payment",
        align = Alignment.MIDDLE,
        fontSize = 35,
    )
    text(
        """
        |Agent Code: ${context.localStorage.agent?.agentCode}
        |Agent Phone: ${request.agentPhoneNumber}
        |--------------------------
        |Amount NGN${request.amount}
        |Payment Item: ${request.paymentItemName}
        |Payment Item Code: ${request.paymentItemCode}
        |Biller Name: ${request.billerName}
        |Biller Category: ${request.billerCategoryName}
        |Transaction Date: $transactionDate
        |Customer Account: ${request.customerId?.mask(4, 2)}
        |Customer Name: ${request.customerName}
        |RRN: ${request.deviceNumber}${request.retrievalReferenceNumber}
        |Transaction ID: ${request.customerDepositSlipNumber}
        """.trimMargin()
    )

    if (!response?.additionalInformation.isNullOrBlank()) {
        val additionalInfo = defaultJson.decodeFromString(
            deserializer = PayBillResponse.AdditionalInformation.serializer(),
            string = response!!.additionalInformation!!,
        )

        text("Customer Address: ${additionalInfo.customerAddress}")
        text("Customer Token: ${additionalInfo.customerToken}")
    }

    transactionStatus(
        context = context,
        isSuccessful = response?.isSuccessful == true,
        responseCode = response?.responseCode ?: if (response!!.isSuccessful) "00" else "06",
        reason = response?.responseMessage,
    )
    footer(context)
}

fun posCashoutReceipt(
    context: Context,
    request: POSCashoutRequest,
    transactionDate: String,
    settlementDate: String?,
    isSuccessful: Boolean = false,
    reason: String? = null,
    responseCode: String? = null
) = printJob {

    logo()
    text(
        text = "POS CASHOUT",
        align = Alignment.MIDDLE,
        fontSize = 35,
    )
    text(
        """
        |Agent Code: ${context.localStorage.agent?.agentCode}
        |Agent Phone: ${request.agentPhoneNumber}
        |--------------------------
        |Amount: NGN${request.amount}
        |Transaction Ref: ${request.transactionReference}
        |Settlement Date: $settlementDate
        |Transaction Date: $transactionDate
        |Masked Pan : ${request.maskedPan}
        |Transaction Stan: ${request.transactionStan}
        |CardType : ${request.cardType}
        |Expiry Date: ${request.expiryDate}
        |CardHolder Name: ${request.cardHolderName}
        |RRN: ${request.retrievalReferenceNumber}
        """.trimMargin()
    )
    transactionStatus(
        context = context,
        isSuccessful = isSuccessful,
        responseCode = responseCode!!,
        reason = reason,
    )
    footer(context)
}

fun fundsTransferReceipt(
    context: Context,
    request: FundsTransferRequest,
    transactionDate: String,
    isSuccessful: Boolean = false,
    reason: String? = null,
    responseCode: String? = null
) = printJob {
    val beneficiary =
        "${request.beneficiaryAccountName} ${request.beneficiaryAccountNumber.mask(4, 2)}"

    logo()
    text(
        text = "Funds Transfer",
        align = Alignment.MIDDLE,
        fontSize = 35,
    )
    text(
        """
        |Agent Code: ${context.localStorage.agent?.agentCode}
        |Agent Phone: ${request.agentPhoneNumber}
        |--------------------------
        |Amount NGN${request.amountInNaira}
        |Beneficiary: $beneficiary
        |Transaction Date: $transactionDate
        |Narration: ${request.narration}
        |RRN: ${request.deviceNumber}${request.retrievalReferenceNumber}""".trimMargin()
    )
    transactionStatus(
        context = context,
        isSuccessful = isSuccessful,
        responseCode = responseCode ?: if (isSuccessful) "00" else "06",
        reason = reason,
    )
    footer(context)
}

fun newAccountReceipt(
    context: Context,
    bvn: String? = null,
    accountNumber: String? = "",
    uniqueReferenceID: String? = null,
    accountName: String? = null,
    isSuccessful: Boolean = false,
    reason: String? = null,
    responseCode: String? = null
) = printJob {
    val isWalletAccount = bvn.isNullOrEmpty()
    val title = if (isWalletAccount) "New Wallet" else "New Account"
    val agent = context.localStorage.agent

    logo()
    text(
        title,
        align = Alignment.MIDDLE,
        fontSize = 35,
    )
    text(
        """
        |Agent Code: ${agent?.agentCode}
        |Agent Phone: ${agent?.phoneNumber}
        |--------------------------
        |Account Name: $accountName
        |Account Number: ${accountNumber.mask(4, 2)}
        |Account Type: ${if (isWalletAccount) "Wallet Account" else "Savings Account"}
        |
        |Creation Date: ${Instant.now().toString("dd-MM-yyyy hh:mm")}
        |RRN: $uniqueReferenceID""".trimMargin()
    )
    transactionStatus(
        context = context,
        isSuccessful = isSuccessful,
        responseCode = responseCode ?: if (isSuccessful) "00" else "06",
        reason = reason,
    )
    footer(context)
}

fun PrintJobScope.footer(context: Context) {
    text(
        text = "-----------------------------", align = Alignment.MIDDLE,
        fontSize = 15,
    )
    text(
        text = "${context.getString(R.string.app_name)} " +
                "v${BuildConfig.VERSION_NAME}. " +
                "Powered by ${context.getString(R.string.institution_name)}",
        align = Alignment.MIDDLE,
        fontSize = 15,
    )
    text(
        text = context.getString(R.string.institution_website),
        align = Alignment.MIDDLE,
        walkPaperAfterPrint = 10,
        fontSize = 15,
    )
}