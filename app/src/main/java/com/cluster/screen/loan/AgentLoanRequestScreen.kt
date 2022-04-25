package com.cluster.screen.loan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cluster.core.data.api.AgentLoansService
import com.cluster.core.data.model.AgentLoanRequest
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.util.safeRunIO
import com.cluster.core.util.toCurrencyFormat
import com.cluster.ui.*
import com.cluster.viewmodel.AppViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun AgentLoanRequestScreen(
    navController: NavController
) {
    val appViewModel: AppViewModel = viewModel()
    val dialogProvider by rememberDialogProvider()
    val loan by appViewModel.agentLoan.collectAsState()
    var loadingMessage by remember { mutableStateOf("") }
    var activeJob: Job? by remember { mutableStateOf(null) }
    val coroutineScope = rememberCoroutineScope()
    val localStorage: LocalStorage by rememberBean()
    var amountString by remember { mutableStateOf("") }
    val amountIsValid = remember(amountString, loan) {
        with(amountString.toDoubleOrNull()) {
            this != null && this > 0.0 && this <= loan!!.maxAmount
        }
    }
    val transactionReference = rememberTransactionReference()
    val agentLoansService: AgentLoansService by rememberRetrofitService()
    val amount = remember(amountString) { if (amountIsValid) amountString.toDouble() else 0.0 }

    val requestLoan = suspend requestLoan@{
        val notice = """
            |1. You are about to request for loan liquidity support. 
            |2. Your loan offer is ${amount.toCurrencyFormat()}
            |3. A processing fee of ${loan!!.feeRate}% will be surcharged.
            |4. The automatic repayment will be processed after 24hrs.
        """.trimMargin()
        val shouldProceed = dialogProvider.getConfirmation(
            title = "Kindly Note",
            subtitle = notice,
        )
        if (!shouldProceed) {
            return@requestLoan
        }
        val pin = dialogProvider.getAgentPin() ?: return@requestLoan

        loadingMessage = "Processing request"
        val agentLoanRequest = AgentLoanRequest(
            institutionCode = localStorage.institutionCode,
            agentPhoneNumber = localStorage.agentPhone,
            agentPin = pin,
            amount = amount,
            geoLocation = localStorage.lastKnownLocation,
            loanProductId = loan!!.loanProductId,
            tenure = loan!!.tenure,
            deviceNumber = localStorage.deviceNumber,
            feeAmount = loan!!.feeRate * amount / 100.0,
            requestReference = transactionReference,
            retrievalReferenceNumber = transactionReference,
        )
        val (response, error) = safeRunIO {
            agentLoansService.process(agentLoanRequest)
        }
        loadingMessage = ""

        if (error != null) {
            dialogProvider.showErrorAndWait(error)
            return@requestLoan
        }
        if (response!!.isFailure()) {
            dialogProvider.showErrorAndWait(response.responseMessage!!)
            return@requestLoan
        }

        dialogProvider.showSuccessAndWait(response.responseMessage ?: "Successful")
        navController.popBackStack()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CreditClubAppBar(
            title = "Request Loan",
            onBackPressed = { navController.popBackStack() },
        )
        if (loadingMessage.isNotBlank()) {
            Loading(
                message = loadingMessage,
                onCancel = {
                    activeJob?.cancel()
                    activeJob = null
                    loadingMessage = ""
                },
            )
            return@Column
        }


        OutlinedTextField(
            label = { Text(text = "Amount") },
            value = amountString,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            onValueChange = { amountString = it },
            maxLines = 1,
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            singleLine = true,
            isError = amountString.isNotBlank() && !amountIsValid,
        )
        Text(
            text = "Can be up to ${loan!!.maxAmount.toCurrencyFormat()}",
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .padding(top = 3.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
        )

        Spacer(modifier = Modifier.padding(top = 0.dp))

        if (loadingMessage.isBlank() && amountIsValid) {
            AppButton(
                onClick = {
                    activeJob = coroutineScope.launch {
                        requestLoan()
                    }
                },
            ) {
                Text(text = "Submit")
            }
        }
    }
}

