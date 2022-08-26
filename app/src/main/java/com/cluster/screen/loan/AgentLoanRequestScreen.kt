package com.cluster.screen.loan

import android.content.Context
import android.os.Bundle
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
import com.cluster.utility.roundTo2dp
import com.cluster.viewmodel.AppViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AgentLoanRequestScreen(
    navController: NavController,
    context: Context
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
    val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(context) }
    val transactionReference = rememberTransactionReference()
    val agentLoansService: AgentLoansService by rememberRetrofitService()
    val amount = remember(amountString) { if (amountIsValid) amountString.toDouble() else 0.0 }
    val currentLoanTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun requestLoan(){
        val notice = """
            |1. You are about to request for an overdraft. 
            |2. Your overdraft offer is ${amount.toCurrencyFormat()}
            |3. You will be able to access ${amount.toCurrencyFormat()} more than your current balance.
            |4. This is not a Loan.
            |5. A processing fee of ${loan!!.feeRate}% will be charged upfront.
            |6. The overdraft amount will be available for use within 24hrs.
            |7. The automatic repayment will be processed after 24hrs.
            |8. A defaulting fee of ${loan!!.interest}% will be charged daily if Overdraft isn't paid fully after 24hrs.
        """.trimMargin()
        val shouldProceed = dialogProvider.getLoanConfirmation(
            title = "Kindly Note",
            subtitle = notice
        )
        if (!shouldProceed) {
            return
        }
        val pin = dialogProvider.getAgentPin() ?: return

        val agentLoanRequest = AgentLoanRequest(
            institutionCode = localStorage.institutionCode,
            agentPhoneNumber = localStorage.agentPhone,
            agentPin = pin,
            amount = amount,
            geoLocation = localStorage.lastKnownLocation,
            loanProductId = loan!!.loanProductId,
            tenure = loan!!.tenure,
            deviceNumber = localStorage.deviceNumber,
            feeAmount = ((loan!!.feeRate * amount) / 100.0).roundTo2dp(),
            requestReference = transactionReference,
            retrievalReferenceNumber = transactionReference,
        )

        dialogProvider.showProgressBar("Processing request")
        val (response, error) = safeRunIO {
            agentLoansService.process(agentLoanRequest)
        }

        dialogProvider.hideProgressBar()

        if (error != null) {
            dialogProvider.showErrorAndWait(error)
            return
        }
        if (response!!.isFailure()) {
            dialogProvider.showErrorAndWait(response.responseMessage!!)
            return
        }

        /*val newAgentLoanEligibility = localStorage.agentLoanEligibility?.copy(isEligible = false)
        localStorage.agentLoanEligibility = newAgentLoanEligibility
        appViewModel.agentLoan.value = newAgentLoanEligibility*/
        dialogProvider.showSuccessAndWait(response.responseMessage ?: "Successful")
        navController.popBackStack()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CreditClubAppBar(
            title = "Request Overdraft",
            onBackPressed =
            { navController.popBackStack()
                firebaseAnalytics.logEvent("OnExitLoan", Bundle().apply {
                    firebaseAnalytics.setUserId(localStorage.agent!!.agentCode)
                    putString("activity_type", "Loan Exit")
                    putString("loan_exit_time", currentLoanTime.format(formatter))
                })
            },
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

        firebaseAnalytics.logEvent("OnEntryLoan", Bundle().apply {
            firebaseAnalytics.setUserId(localStorage.agent!!.agentCode)
            putString("activity_type", "Loan Entry")
            putString("loan_start_time", currentLoanTime.format(formatter))

        })

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
            text = "You can borrow up to ${loan!!.maxAmount.toCurrencyFormat()}",
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

