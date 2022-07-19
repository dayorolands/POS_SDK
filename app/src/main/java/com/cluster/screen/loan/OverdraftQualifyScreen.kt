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
import com.cluster.viewmodel.AppViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun OverdraftQualifyScreen(
    navController: NavController,
    context: Context
) {
    val dialogProvider by rememberDialogProvider()
    var loadingMessage by remember { mutableStateOf("") }
    var activeJob: Job? by remember { mutableStateOf(null) }
    val coroutineScope = rememberCoroutineScope()
    val localStorage: LocalStorage by rememberBean()

    val firebaseAnalytics by lazy { FirebaseAnalytics.getInstance(context) }
    val currentLoanTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    val requestOverdraft = suspend requestOverdraft@{
        val notice = """
            |1. Be an active agent for a minimum of 3 months. 
            |2. Have a verifiable/tagged business location for full KYC.
            |3. Consistently transaction month on month for 3 months.
            |4. Achieve a consistent monthly transaction average inflow of N1,000,000 minimum.
        """.trimMargin()

        val shouldProceed = dialogProvider.getOverdraftConfirmation(
            title = "To Qualify for an Overdraft",
            subtitle = notice
        )
        if (!shouldProceed) {
            return@requestOverdraft
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CreditClubAppBar(
            title = "Request Overdraft",
            onBackPressed =
            { navController.popBackStack()
                firebaseAnalytics.logEvent("OnExitOverdraftQualify", Bundle().apply {
                    firebaseAnalytics.setUserId(localStorage.agent!!.agentCode)
                    putString("activity_type", "Overdraft Qualify Exit")
                    putString("overdraft_qualify_exit_time", currentLoanTime.format(formatter))
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

        firebaseAnalytics.logEvent("OnEntryOverdraftQualify", Bundle().apply {
            firebaseAnalytics.setUserId(localStorage.agent!!.agentCode)
            putString("activity_type", "Overdraft Qualify Entry")
            putString("overdraft_qualify_start_time", currentLoanTime.format(formatter))

        })

        Text(
            text = "You are not eligible for an overdraft",
            style = MaterialTheme.typography.body1,
            modifier = Modifier
                .padding(top = 10.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
        )

        Spacer(modifier = Modifier.padding(top = 5.dp))

        AppButton(
            onClick = {
                activeJob = coroutineScope.launch {
                    requestOverdraft()
                }
            },
        ) {
            Text(text = "HOW CAN I BECOME ELIGIBLE?")
        }
    }
}

