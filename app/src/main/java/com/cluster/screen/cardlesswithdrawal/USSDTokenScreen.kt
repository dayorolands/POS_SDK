package com.cluster.screen.cardlesswithdrawal

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
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.core.util.*
import com.cluster.ui.*
import kotlinx.coroutines.launch

@Composable
fun USSDTokenScreen(
    navController: NavController,
    dialogProvider: DialogProvider
){
    val coroutineScope = rememberCoroutineScope()
    var tokenString by remember{ mutableStateOf("") }
    var agentPin by remember{ mutableStateOf("") }
    val agentPinDigit = remember(agentPin) { agentPin }
    val token = remember(tokenString){ tokenString}
    val tokenIsValid = tokenString.isNotBlank()
    var getTransactionDetails: GetTransactionDetails? by remember{ mutableStateOf(null) }
    val localStorage : LocalStorage by rememberBean()
    val bankCode = remember(getTransactionDetails){
        if(getTransactionDetails != null) getTransactionDetails!!.bankCode else null
    }
    var showConfirmation by remember(bankCode){ mutableStateOf(false) }
    val cardlessWithdrawalService : CardlessWithdrawalService by rememberRetrofitService()
    val context = LocalContext.current
    var loadingMessage by remember{ mutableStateOf("") }
    val title = when{
        getTransactionDetails == null -> stringResource(id = R.string.ussd_token_withdrawal)
        showConfirmation -> stringResource(id = R.string.complete_transaction)
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

    val getTransDetailsCall: SuspendCallback =
        remember(tokenString){
            getTransDetailsCall@{
                errorMessage = ""
                if (agentPin.isBlank()) {
                    val pin = dialogProvider.getPin("Agent PIN") ?: return@getTransDetailsCall
                    if (pin.isEmpty()) return@getTransDetailsCall dialogProvider.showError("Please enter your PIN")
                    if (pin.length != 4) return@getTransDetailsCall dialogProvider.showError("PIN must be four digits")
                    agentPin = pin
                }
                loadingMessage = context.getString(R.string.loading_message)
                val(response, error) = safeRunIO {
                    cardlessWithdrawalService.getTransactionDetails(
                        agentToken = token,
                        agentCode = localStorage.agent!!.agentCode
                    )
                }
                loadingMessage = ""
                if(error != null) errorMessage = error.getMessage(context)
                if(response == null) return@getTransDetailsCall
                if(response.isFailure()){
                    errorMessage = response.message
                    return@getTransDetailsCall
                }
                getTransactionDetails = response.data
            }
        }

    val bankName = getTransactionDetails?.bankCode ?: "Unknown Bank"
    val accountNumber = getTransactionDetails?.accountNumber ?: "1234567890"
    val accountName = getTransactionDetails?.name ?: "No name"
    val formattedAmount = remember(getTransactionDetails){
        if(getTransactionDetails?.amount != null) getTransactionDetails?.amount!!.toCurrencyFormat() else ""
    }

    Column {
        CreditClubAppBar(
            title = title,
            onBackPressed = {
                navController.popBackStack()
            }
        )

        LazyColumn(modifier = Modifier.weight(1f)){
            if(!showConfirmation) {
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(
                        text = AnnotatedString(text = requiredText, spanStyles = spanStyles),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .wrapContentWidth()
                            .align(Alignment.CenterHorizontally)
                            .padding(20.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.agent_code),
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

            if(loadingMessage.isBlank() && bankCode != null){
                item(key = "confirm-bank") {
                    DataItem(
                        label = "Bank",
                        value = bankName
                    )
                }
                item(key = "confirm-accountNumber"){
                    DataItem(
                        label = "Account Number",
                        value = accountNumber
                    )
                }
                item(key = "confirm-accountName"){
                    DataItem(
                        label = "Account Name",
                        value = accountName
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
            AppButton(
                onClick = { coroutineScope.launch { if(getTransactionDetails == null) getTransDetailsCall() else getTransDetailsCall()} }
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