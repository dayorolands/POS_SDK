package com.cluster.screen.cardlesswithdrawal

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.cluster.R
import com.cluster.core.data.api.CardlessWithdrawalService
import com.cluster.core.data.model.GetTransactionDetails
import com.cluster.core.data.model.SubmitTokenRequest
import com.cluster.core.data.model.SubmitTokenResponse
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.data.prefs.newTransactionReference
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.core.util.*
import com.cluster.fragment.navigateToReceipt
import com.cluster.pos.printer.PrintJob
import com.cluster.receipt.tokenWithdrawalReceipt
import com.cluster.screen.ReceiptDetails
import com.cluster.ui.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.Instant

@Composable
fun USSDTokenScreen(
    navController: NavController,
    dialogProvider: DialogProvider
){
    val coroutineScope = rememberCoroutineScope()
    var tokenString by remember{ mutableStateOf("") }
    var assignPin by remember{ mutableStateOf("") }
    var doneLoading by remember{ mutableStateOf(false) }
    val token = remember(tokenString){ tokenString}
    val uniqueReference = rememberTransactionReference()
    var getTransactionDetails: GetTransactionDetails? by remember{ mutableStateOf(null) }
    val tokenIsActive = remember(getTransactionDetails != null){
        getTransactionDetails?.tokenStatus == 0
    }
    var receipt: PrintJob? by remember { mutableStateOf(null) }
    val transactionPending by remember { mutableStateOf(false)}
    val tokenIsValid = tokenString.isNotBlank() && tokenString.length == 5
    val localStorage : LocalStorage by rememberBean()
    var showConfirmation by remember(getTransactionDetails){ mutableStateOf(false) }
    val cardlessWithdrawalService : CardlessWithdrawalService by rememberRetrofitService()
    val context = LocalContext.current
    var loadingMessage by remember{ mutableStateOf("") }
    val title = when{
        getTransactionDetails == null -> stringResource(id = R.string.ussd_token_withdrawal)
        tokenIsActive -> stringResource(id = R.string.complete_transaction)
        else -> stringResource(id = R.string.ussd_token_withdrawal)
    }
    var errorMessage : String? by remember{ mutableStateOf(null) }
    val placeholder = stringResource(id = R.string.placeholder)
    val requiredText = stringResource(id = R.string.customerText, placeholder)
    val startIndex = requiredText.indexOf(placeholder)
    val spanStyles = listOf(
        AnnotatedString.Range(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 25.sp),
            start = startIndex,
            end = startIndex + placeholder.length
        )
    )

    val getTransDetailsCall: suspend CoroutineScope.() -> Unit =
        remember(tokenString){
            getTransDetailsCall@{
                errorMessage = ""
//                if (assignPin.isBlank()) {
//                    val pin = dialogProvider.getPin("Agent PIN") ?: return@getTransDetailsCall
//                    if (pin.isEmpty()) return@getTransDetailsCall dialogProvider.showError("Please enter your PIN")
//                    if (pin.length != 4) return@getTransDetailsCall dialogProvider.showError("PIN must be four digits")
//                    assignPin = pin
//                }
                loadingMessage = context.getString(R.string.loading_message)
                val(response, error) = safeRunIO {
                    cardlessWithdrawalService.getTransactionDetails(
                        channel = LocalStorage.USSD_CHANNEL,
                        agentToken = token,
                        agentCode = localStorage.agent!!.agentCode
                    )
                }
                loadingMessage = ""
                if (error != null) {
                    if (error is SerializationException || error.isKotlinNPE()) {
                        errorMessage = "Unable to get transaction details"
                        return@getTransDetailsCall
                    }
                    errorMessage = error.getMessage(context)
                    return@getTransDetailsCall
                }
                if(response == null){
                    errorMessage = "Invalid token"
                    return@getTransDetailsCall
                }
                if(!response.isUssdSuccess()){
                    errorMessage = response.message
                    return@getTransDetailsCall
                }
                if(response.data?.tokenStatus == 1){
                    errorMessage = response.message
                    return@getTransDetailsCall
                }
                getTransactionDetails = response.data
                showConfirmation = true
                doneLoading = true
            }
        }

    val phoneNumber = getTransactionDetails?.phoneNumber ?: "No Phone Number"
    val accountNumber = getTransactionDetails?.accountNumber ?: "1234567890"
    val bankCode = getTransactionDetails?.bankCode
    val bankName = getTransactionDetails?.bank ?: "No bank name"
    val accountName = getTransactionDetails?.accountName ?: "No customer name"
    val formattedAmount = remember(getTransactionDetails){
        if(getTransactionDetails?.amount != null) getTransactionDetails?.amount!!.toCurrencyFormat() else ""
    }

    val proceedTransaction: suspend CoroutineScope.() -> Unit =
        proceedTransaction@{
            if (assignPin.isBlank()) {
                val pin = dialogProvider.getPin("Agent PIN") ?: return@proceedTransaction
                if (pin.isEmpty()) return@proceedTransaction dialogProvider.showError("Please enter your PIN")
                if (pin.length != 4) return@proceedTransaction dialogProvider.showError("PIN must be four digits")
                assignPin = pin
            }
            val serializer = SubmitTokenRequest.Additional.serializer()
            val agent = localStorage.agent
            val additional = SubmitTokenRequest.Additional().apply {
                agentCode = agent?.agentCode
                terminalId = agent?.terminalID
            }
            val submitTokenRequest = SubmitTokenRequest().apply {
                institutionCode = localStorage.institutionCode
                agentPhoneNumber = localStorage.agentPhone
                customerAccountNumber = accountNumber
                amount = getTransactionDetails?.amount.toString()
                agentPin = assignPin
                customerToken = tokenString
                geoLocation = localStorage.lastKnownLocation
                retrievalReferenceNumber = localStorage.newTransactionReference()
                deviceNumber = localStorage.deviceNumber
                additionalInformation = Json.encodeToString(serializer, additional)
                destinationBankCode = bankCode
                requestReference = uniqueReference
            }

            dialogProvider.showProgressBar("Transaction Processing..", isCancellable = true) {
                onClose {
                    cancel()
                }
            }

            val(response, error) = safeRunIO {
                cardlessWithdrawalService.confirmToken(
                    channel = LocalStorage.USSD_CHANNEL,
                    request = submitTokenRequest
                )
            }

            dialogProvider.hideProgressBar()

            if (error != null) {
                dialogProvider.showErrorAndWait(error)
                return@proceedTransaction
            }

            if (response == null) {
                dialogProvider.showErrorAndWait("A network-related error occurred while confirming token")
                return@proceedTransaction
            }

            receipt = tokenWithdrawalReceipt(
                context = context,
                request = submitTokenRequest,
                response = response,
                transactionDate = Instant.now().toString("dd-MM-yyyy hh:mm"),
                customerName = accountName,
                bankName = bankName
            )
        }

    if (receipt != null && loadingMessage.isBlank()) {
        ReceiptDetails(
            navController = navController,
            printJob = receipt!!,
            transactionPending = transactionPending
        )
        return
    }
    

    Column {
        CreditClubAppBar(
            title = title,
            onBackPressed = {
                navController.popBackStack()
            }
        )

        LazyColumn(modifier = Modifier.weight(1f)){
            if(!tokenIsActive) {
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(
                        text = stringResource(id = R.string.customerText),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .wrapContentWidth()
                            .align(Alignment.CenterHorizontally)
                            .padding(20.dp)
                    )
                    val agentCode = stringResource(id = R.string.placeholder) + "${localStorage.agent!!.agentCode}" + "*Amount#"
                    Text(
                        text = agentCode,
                        fontSize = 25.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally),
                    )
                    OutlinedTextField(
                        label = { Text(text = stringResource(id = R.string.token)) },
                        value = tokenString,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        onValueChange = { tokenString = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, end = 20.dp, start = 20.dp),
                        singleLine = true,
                        enabled = getTransactionDetails == null && loadingMessage.isBlank()
                    )
                }
                if(loadingMessage.isNotBlank()){
                    item{
                        Loading(message = loadingMessage)
                    }
                }
                if(!errorMessage.isNullOrBlank()){
                    item{
                        ErrorMessage(content = errorMessage!!)
                    }
                }
            }
            if(tokenIsActive){
                item(key = "confirm-bank") {
                    DataItem(
                        label = "Bank",
                        value = bankName
                    )
                }
                item(key = "confirm-accountName"){
                    DataItem(
                        label = "Account Name",
                        value = accountName
                    )
                }
                item(key = "confirm-accountNumber"){
                    DataItem(
                        label = "Account Number",
                        value = accountNumber
                    )
                }
                item(key = "confirm-phoneNumber"){
                    DataItem(
                        label = "Phone Number",
                        value = phoneNumber
                    )
                }
                item(key = "Amount"){
                    DataItem(
                        label = "Amount",
                        value = formattedAmount
                    )
                }
            }
        }
        
        if(loadingMessage.isBlank() && tokenIsValid){
            AppUssdButton(
                onClick = {
                    coroutineScope.launch {
                        if(getTransactionDetails == null)
                            getTransDetailsCall()
                        else
                            proceedTransaction()
                    }
                }
            ) {
                Text(
                    text = if(getTransactionDetails == null) {
                        stringResource(id = R.string.confirm_otp)
                    } else{
                        stringResource(id = R.string.proceed)
                    }
                )
            }
        }
    }

}