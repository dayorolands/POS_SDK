package com.creditclub.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.receipt.footerNodes
import com.appzonegroup.app.fasttrack.ui.ReceiptDetails
import com.appzonegroup.app.fasttrack.utility.FunctionUsageTracker
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.creditclub.core.data.api.UssdCashoutService
import com.creditclub.core.data.model.CoraPayReference
import com.creditclub.core.data.model.CoraPayTransactionStatus
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.data.request.CoralPayReferenceRequest
import com.creditclub.core.util.SuspendCallback
import com.creditclub.core.util.getMessage
import com.creditclub.core.util.safeRunIO
import com.creditclub.pos.printer.ParcelablePrintJob
import com.creditclub.pos.printer.printJob
import com.creditclub.ui.*
import kotlinx.coroutines.launch
import java.util.*


@Composable
fun UssdWithdrawal(navController: NavController) {
    FunctionUsageTracker(fid = FunctionIds.USSD_WITHDRAWAL)

    val coroutineScope = rememberCoroutineScope()
    var loadingMessage by remember { mutableStateOf("") }
    var errorMessage: String? by remember { mutableStateOf(null) }
    val context = LocalContext.current
    val ussdCashoutService: UssdCashoutService by rememberRetrofitService()
    val localStorage: LocalStorage by rememberBean()
    val dialogProvider by rememberDialogProvider()

    var reference: CoraPayReference? by remember { mutableStateOf(null) }
    var printJob: ParcelablePrintJob? by remember { mutableStateOf(null) }
    var amountString by remember { mutableStateOf("") }
    val amount = remember(amountString) { amountString.toIntOrNull() ?: 0 }
    val amountIsValid = amount > 0
    val ussdCode = remember(reference) {
        if (reference != null) reference!!.transactionReference else null
    }

    val generateReference: SuspendCallback =
        remember(amountString) {
            generateReference@{
                val agentPin = dialogProvider.getPin(R.string.agent_pin) ?: return@generateReference
                errorMessage = ""
                loadingMessage = context.getString(R.string.processing)
                val (response, error) = safeRunIO {
                    ussdCashoutService.generateReference(
                        CoralPayReferenceRequest(
                            institutionCode = localStorage.institutionCode!!,
                            agentPhoneNumber = localStorage.agentPhone!!,
                            agentPin = agentPin,
                            amount = amount,
                            geoLocation = localStorage.lastKnownLocation,
                        )
                    )
                }
                loadingMessage = ""
                if (error != null) errorMessage = error.getMessage(context)
                if (response == null) return@generateReference
                if (response.isFailure()) {
                    errorMessage = response.message
                    return@generateReference
                }

                reference = response.data
            }
        }

    val checkTransactionStatus: SuspendCallback =
        remember(amountString) {
            checkTransactionStatus@{
                errorMessage = ""
                loadingMessage = context.getString(R.string.checking_transaction_status)
                val (response, error) = safeRunIO {
                    ussdCashoutService.getTransactionStatus(
                        requestReference = reference!!.requestReference,
                        institutionCode = localStorage.institutionCode,
                    )
                }
                loadingMessage = ""
                if (error != null) errorMessage = error.getMessage(context)
                if (response == null) return@checkTransactionStatus
                if (response.isFailure()) {
                    errorMessage = response.message
                    return@checkTransactionStatus
                }
                val status = response.data?.data
                if (status == CoraPayTransactionStatus.Pending) {
                    Toast.makeText(context, "Transaction is still pending", Toast.LENGTH_SHORT)
                        .show()
                    return@checkTransactionStatus
                }
                val statusText = when (status) {
                    CoraPayTransactionStatus.Pending -> "Pending"
                    CoraPayTransactionStatus.Failed -> "Failed"
                    CoraPayTransactionStatus.Successful -> "Successful"
                    CoraPayTransactionStatus.Reversed -> "Reversed"
                    CoraPayTransactionStatus.ThirdPartyFailure -> "Third Party Failure"
                    CoraPayTransactionStatus.NotFound -> "Not Found"
                    else -> "Error"
                }
                printJob = printJob {
                    val middleAlignment = com.creditclub.pos.printer.Alignment.MIDDLE
                    image(R.drawable.cc_printer_logo)
                    text("USSD withdrawal", fontSize = 35, align = middleAlignment)
                    text(
                        statusText,
                        align = middleAlignment,
                    )
                    text(
                        response.message?.uppercase(Locale.getDefault()) ?: "",
                        fontSize = 15,
                        walkPaperAfterPrint = 20,
                        align = middleAlignment,
                    )
                    if (reference != null) {
                        text(
                            "transaction reference: ${reference?.transactionReference}",
                            align = middleAlignment,
                        )
                        text(
                            "request reference: ${reference?.transactionReference}",
                            align = middleAlignment,
                        )
                    }
                    footerNodes(context)
                }
            }
        }

    if (printJob != null) {
        ReceiptDetails(
            navController = navController,
            printJob = printJob!!,
        )
        return
    }

    Column {
        CreditClubAppBar(
            title = stringResource(R.string.ussd_withdrawal),
            onBackPressed = { navController.popBackStack() },
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                OutlinedTextField(
                    label = { Text(text = stringResource(id = R.string.amount)) },
                    value = amountString,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    onValueChange = { amountString = it },
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
                    singleLine = true,
                    enabled = reference == null && loadingMessage.isBlank(),
                )
            }
            if (loadingMessage.isBlank() && ussdCode != null) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {

                            },
                    ) {
                        Text(
                            text = ussdCode,
                            fontSize = 40.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(top = 20.dp),
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 20.dp),
                        ) {
                            Text(
                                text = "Tap to copy",
                                fontSize = 20.sp,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(end = 10.dp),
                            )
                            Icon(
                                imageVector = Icons.Outlined.CopyAll,
                                contentDescription = null,
                                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                            )
                        }
                    }
                }
            }
            if (loadingMessage.isNotBlank()) {
                item {
                    Loading(message = loadingMessage)
                }
            }
            if (!errorMessage.isNullOrBlank()) {
                item {
                    ErrorMessage(errorMessage!!)
                }
            }
        }

        if (loadingMessage.isBlank() && amountIsValid) {
            AppButton(
                onClick = { coroutineScope.launch { if (reference == null) generateReference() else checkTransactionStatus() } },
            ) {
                Text(
                    text = if (reference == null) {
                        stringResource(R.string.generate_reference)
                    } else {
                        stringResource(R.string.ive_made_payment)
                    }
                )
            }
        }
    }
}
