package com.cluster.screen

import android.content.SharedPreferences
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cluster.R
import com.cluster.core.data.api.PayWithTransferService
import com.cluster.core.data.model.Additional
import com.cluster.core.data.model.GetFee
import com.cluster.core.data.model.InitiatePayment
import com.cluster.core.data.model.InitiatePaymentRequest
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.data.prefs.newTransactionReference
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.core.util.getMessage
import com.cluster.core.util.isKotlinNPE
import com.cluster.core.util.safeRunIO
import com.cluster.ui.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Created by Ifedayo Adekoya <iadekoya@appzonegroup.com> on 29/11/2022.
 * Appzone Ltd
 */

@Composable
fun PayWithTransfer(
    navController: NavController,
    dialogProvider : DialogProvider
){
    var amountString by remember{ mutableStateOf("") }
    val amountIsValid = amountString.isNotBlank()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val payWithTransferService : PayWithTransferService by rememberRetrofitService()
    var getFee : GetFee? by remember{ mutableStateOf(null) }
    val isCalculated = getFee != null
    val localStorage : LocalStorage by rememberBean()
    var errorMessage: String? by remember{ mutableStateOf(null) }
    var loadingMessage by remember{ mutableStateOf("") }
    var assignPin by remember{ mutableStateOf("") }
    var initiatePaymentResponse : InitiatePayment? by remember{ mutableStateOf(null) }


    val initiatePayment: suspend CoroutineScope.() -> Unit =
        remember(amountString){
            initiatePayment@{
                errorMessage = ""
                if (assignPin.isBlank()) {
                    val pin = dialogProvider.getPin("Agent PIN") ?: return@initiatePayment
                    if (pin.isEmpty()) return@initiatePayment dialogProvider.showError("Please enter your PIN")
                    if (pin.length != 4) return@initiatePayment dialogProvider.showError("PIN must be four digits")
                    assignPin = pin
                }
                val serializer = Additional.serializer()
                val agent = localStorage.agent
                var additional = Additional()
                additional.agentCode = agent?.agentCode
                additional.terminalId = agent?.terminalID

                val initiatePaymentRequest = InitiatePaymentRequest(
                    agentPhoneNumber = agent?.phoneNumber,
                    institutionCode = "100616",
                    agentPin = assignPin,
                    customerName = "Ifedayo Adekoya",
                    retrievalReferenceNumber = localStorage.newTransactionReference(),
                    geolocation = localStorage.lastKnownLocation,
                    deviceNumber = localStorage.deviceNumber,
                    amount = amountString.toInt(),
                    additionalInformation = Json.encodeToString(serializer, additional)
                )

                dialogProvider.showProgressBar("Generating Payment Details..", isCancellable = true) {
                    onClose {
                        cancel()
                    }
                }

                val (response, error) = safeRunIO {
                    payWithTransferService.initiatePayment(
                        initiatePaymentRequest
                    )
                }

                dialogProvider.hideProgressBar()

                if (error != null) {
                    dialogProvider.showErrorAndWait(error)
                    return@initiatePayment
                }

                if (response == null) {
                    dialogProvider.showErrorAndWait("A network-related error occurred while getting details")
                    return@initiatePayment
                }
                if(!response.isUssdSuccess()){
                    dialogProvider.showErrorAndWait(response.message!!)
                    return@initiatePayment
                }
                initiatePaymentResponse = response.data
            }
        }

    if(loadingMessage.isBlank() && initiatePaymentResponse != null){
        PayWithTransferDetails(
            amount = amountString.toInt(),
            navController = navController,
            initiatePaymentResponse = initiatePaymentResponse!!
        )
        return
    }

    Column() {
        CreditClubAppBar(
            title = stringResource(id = R.string.pay_with_transfer),
            onBackPressed = {
                navController.popBackStack()
            }
        )
        LazyColumn(
            modifier = Modifier.weight(1f)
        ){
            item(key = "amount") {
                OutlinedTextField(
                    label = {Text(text = stringResource(id = R.string.amount))},
                    value = amountString,
                    onValueChange = {
                        amountString = it
                        if(it.length >= 2){
                            coroutineScope.launch {
                                errorMessage = ""
                                val (response, error) = safeRunIO {
                                    payWithTransferService.getAmountFee(
                                        institutionCode = "100616",
                                        phoneNumber = localStorage.agentPhone!!,
                                        amount = it
                                    )
                                }
                                if(error != null){
                                    if (error is SerializationException || error.isKotlinNPE()) {
                                        errorMessage = "Unable to get transaction details"
                                        return@launch
                                    }
                                    errorMessage = error.getMessage(context)
                                }
                                if(response == null){
                                    errorMessage = "No response from server"
                                    return@launch
                                }
                                if(!response.isUssdSuccess()){
                                    errorMessage = response.message
                                    return@launch
                                }
                                getFee = response.data
                            }
                        } },
                    shape = RoundedCornerShape(5.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                )
                val transactionFee = getFee?.totalFee ?: 0
                if(isCalculated){
                    OutlinedTextField(
                        label = { Text(text = stringResource(id = R.string.transaction_fee)) },
                        value = transactionFee.toString(),
                        onValueChange = { transactionFee.toString() },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 20.dp, start = 20.dp),
                        singleLine = true,
                    )
                }
            }
            
            if(!errorMessage.isNullOrBlank()){
                item{
                    ErrorMessage(content = errorMessage!!)
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        if(loadingMessage.isBlank() && getFee != null){
            AppUssdButton(
                onClick = {
                    coroutineScope.launch {
                        initiatePayment()
                    }
                }
            ) {
                Text(text = stringResource(id = R.string.confirm))
            }
        }
    }
}
