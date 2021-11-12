package com.appzonegroup.app.fasttrack.ui

import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.receipt.fundsTransferReceipt
import com.creditclub.core.config.InstitutionConfig
import com.creditclub.core.data.ClusterObjectBox
import com.creditclub.core.data.TRANSACTIONS_CLIENT
import com.creditclub.core.data.api.FundsTransferService
import com.creditclub.core.data.model.Bank
import com.creditclub.core.data.model.PendingTransaction
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.data.request.FundsTransferRequest
import com.creditclub.core.data.response.NameEnquiryResponse
import com.creditclub.core.data.response.RetryPolicy
import com.creditclub.core.type.TransactionType
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.defaultJson
import com.creditclub.pos.printer.PrintJob
import com.creditclub.ui.*
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import java.time.Instant
import java.util.*
import kotlin.math.roundToInt

private const val FUNDS_TRANSFER_AUTH_TOKEN = "95C1D8B4-7589-4F70-8F20-473E89FB5F01"

@Composable
fun FundsTransfer(navController: NavController, dialogProvider: DialogProvider) {
    val context = LocalContext.current
    val fundsTransferService: FundsTransferService by rememberRetrofitService()
    val fundsTransferTransactionService: FundsTransferService by rememberRetrofitService(
        TRANSACTIONS_CLIENT
    )
    val localStorage: LocalStorage by rememberBean()
    val institutionConfig: InstitutionConfig by rememberBean()

    val coroutineScope = rememberCoroutineScope()
    var activeJob: Job? by remember { mutableStateOf(null) }

    var loadingMessage by remember { mutableStateOf("") }
    var isSameBank: Boolean? by rememberSaveable { mutableStateOf(null) }
    var bank: Bank? by remember(isSameBank) { mutableStateOf(null) }
    var receiverAccountNumber by remember(bank) { mutableStateOf("") }
    var amountString by remember(bank) { mutableStateOf("") }
    var nameEnquiryResponse: NameEnquiryResponse? by remember(bank, receiverAccountNumber) {
        mutableStateOf(null)
    }
    val isVerified = nameEnquiryResponse != null
    var narration by remember { mutableStateOf("") }
    var agentPin by remember { mutableStateOf("") }
    var receipt: PrintJob? by remember { mutableStateOf(null) }
    val transactionReference = rememberTransactionReference()
    val accountNumberIsValid = remember(receiverAccountNumber) {
        receiverAccountNumber.isNotBlank() && receiverAccountNumber.length == institutionConfig.bankAccountNumberLength
    }

    // variables for requery
    var transferAttemptCount by remember { mutableStateOf(0) }
    var retryPolicy: RetryPolicy? by remember { mutableStateOf(null) }
    var pendingTransactionId: Long? by remember { mutableStateOf(null) }

    val amountIsValid = remember(amountString) {
        with(amountString.toDoubleOrNull()) {
            this != null && this > 0.0
        }
    }
    val amount = remember(amountString) { if (amountIsValid) amountString.toDouble() else 0.0 }
    val formattedAmount = remember(amount, amountIsValid) {
        if (amountIsValid) amount.toCurrencyFormat() else ""
    }
    var errorMessage by remember(receiverAccountNumber, amountString) {
        mutableStateOf("")
    }
    var showConfirmation by remember(nameEnquiryResponse, isSameBank) { mutableStateOf(false) }
    val title = when {
        isSameBank == null -> stringResource(R.string.funds_transfer)
        !isVerified -> stringResource(R.string.enter_account)
        showConfirmation -> stringResource(R.string.confirm)
        else -> stringResource(R.string.funds_transfer)
    }

    val clusterObjectBox: ClusterObjectBox by rememberBean()
    val pendingTransactionsBox: Box<PendingTransaction> = remember {
        clusterObjectBox.boxStore.boxFor()
    }

    val validateAccount: suspend CoroutineScope.() -> Unit =
        remember(
            isSameBank,
            bank,
            receiverAccountNumber,
            transactionReference,
        ) {
            validateAccount@{
                errorMessage = ""
                val nameEnquiryRequest = FundsTransferRequest(
                    agentCode = localStorage.agent?.agentCode,
                    agentPhoneNumber = localStorage.agentPhone,
                    institutionCode = localStorage.institutionCode,
                    authToken = FUNDS_TRANSFER_AUTH_TOKEN,
                    isToRelatedCommercialBank = isSameBank == true,
                    externalTransactionReference = transactionReference,
                    geoLocation = localStorage.lastKnownLocation,
                    beneficiaryAccountNumber = receiverAccountNumber,
                    beneficiaryInstitutionCode = bank?.code,
                    retrievalReferenceNumber = transactionReference,
                    deviceNumber = localStorage.deviceNumber,
                )

                loadingMessage = "Validating account information"
                val (response, error) = safeRunIO {
                    fundsTransferService.nameEnquiry(nameEnquiryRequest)
                }
                loadingMessage = ""

                if (error != null) {
                    if (error is SerializationException || error.isKotlinNPE()) {
                        errorMessage = "Invalid account number"
                        return@validateAccount
                    }
                    errorMessage = error.getMessage(context)
                    return@validateAccount
                }

                if (response == null) {
                    errorMessage = "Invalid account number"
                    return@validateAccount
                }

                if (!response.status) {
                    errorMessage = response.responseMessage ?: "Invalid account number"
                    return@validateAccount
                }
                nameEnquiryResponse = response
            }
        }

    val transferFunds: suspend CoroutineScope.() -> Unit =
        remember(
            isSameBank,
            bank,
            receiverAccountNumber,
            transactionReference,
            amountString,
            showConfirmation,
            nameEnquiryResponse,
        ) {
            makeTransfer@{
                val isFirstAttempt = transferAttemptCount == 0
                errorMessage = ""
                if (agentPin.isBlank()) {
                    val pin = dialogProvider.getPin("Agent PIN") ?: return@makeTransfer
                    if (pin.isEmpty()) return@makeTransfer dialogProvider.showError("Please enter your PIN")
                    if (pin.length != 4) return@makeTransfer dialogProvider.showError("PIN must be four digits")
                    agentPin = pin
                }
                val agent = localStorage.agent
                val fundsTransferRequest = FundsTransferRequest(
                    agentCode = agent!!.agentCode,
                    agentPhoneNumber = localStorage.agentPhone,
                    institutionCode = localStorage.institutionCode,
                    agentPin = agentPin,
                    authToken = FUNDS_TRANSFER_AUTH_TOKEN,
                    beneficiaryAccountNumber = receiverAccountNumber,
                    amountInNaira = amount,
                    isToRelatedCommercialBank = isSameBank ?: false,
                    externalTransactionReference = transactionReference,
                    geoLocation = localStorage.lastKnownLocation,
                    narration = narration.trim { it <= ' ' },
                    beneficiaryInstitutionCode = bank?.code,
                    beneficiaryAccountName = nameEnquiryResponse!!.beneficiaryAccountName,
                    beneficiaryBVN = nameEnquiryResponse!!.beneficiaryBVN,
                    beneficiaryKYC = nameEnquiryResponse!!.beneficiaryKYC,
                    nameEnquirySessionID = nameEnquiryResponse!!.nameEnquirySessionID,
                    retrievalReferenceNumber = transactionReference,
                    deviceNumber = localStorage.deviceNumber,
                )

                loadingMessage = if (transferAttemptCount > 0) {
                    "Verifying transaction status ($transferAttemptCount)"
                } else {
                    "Transfer in progress"
                }
                val (response, error) = safeRunIO {
                    if (isFirstAttempt) {
                        fundsTransferTransactionService.transfer(fundsTransferRequest)
                    } else {
                        fundsTransferService.requery(fundsTransferRequest)
                    }
                }
                loadingMessage = ""

                // Store pending and unconfirmed transaction for requery
                if ((response == null || response.isPending()) && pendingTransactionId == null) {
                    val pendingTransaction = PendingTransaction(
                        transactionType = if (isSameBank == true) TransactionType.LocalFundsTransfer else TransactionType.FundsTransferCommercialBank,
                        requestJson = defaultJson.encodeToString(
                            FundsTransferRequest.serializer(),
                            fundsTransferRequest,
                        ),
                        accountName = nameEnquiryResponse!!.beneficiaryAccountName!!,
                        accountNumber = receiverAccountNumber,
                        amount = amount,
                        reference = transactionReference,
                        createdAt = Instant.now(),
                        lastCheckedAt = null,
                    )
                    pendingTransactionId = pendingTransactionsBox.put(pendingTransaction)
                }

                if (error != null) {
                    if (error.isTimeout() || error.isInternalServerError()) {
                        delay(3000)
                        retryPolicy = RetryPolicy.AutoRetry
                        transferAttemptCount++
                        return@makeTransfer
                    }
                    errorMessage =
                        error.getMessage(context) + ". Please click the CONFIRM button to try again"

                    return@makeTransfer
                }

                when {
                    response!!.isPendingOnBank() -> {
                        retryPolicy = RetryPolicy.RetryLater
                    }
                    response.isPendingOnMiddleware() -> {
                        delay(3000)
                        retryPolicy = RetryPolicy.AutoRetry
                    }
                    response.isFailure() -> {
                        if (pendingTransactionId != null) {
                            // delete pending transaction once confirmed failed
                            pendingTransactionsBox.remove(pendingTransactionId!!)
                        }

                        retryPolicy = null
                        errorMessage = response.responseMessage ?: ""
                    }
                    response.isSuccess() && pendingTransactionId != null -> {
                        if (pendingTransactionId != null) {
                            // delete pending transaction once confirmed successful
                            pendingTransactionsBox.remove(pendingTransactionId!!)
                        }

                        retryPolicy = null
                    }
                }

                receipt = fundsTransferReceipt(
                    context = context,
                    request = fundsTransferRequest,
                    transactionDate = Instant.now().toString("dd-MM-yyyy hh:mm"),
                    isSuccessful = response!!.isSuccessful,
                    reason = response.responseMessage,
                )
                transferAttemptCount++
            }
        }

    LaunchedEffect(receiverAccountNumber) {
        if (accountNumberIsValid && loadingMessage.isBlank() && nameEnquiryResponse == null) {
            validateAccount()
        }
    }

    // Schedule automatic requery for pending transactions
    LaunchedEffect(transferAttemptCount) {
        if (transferAttemptCount > 0 && retryPolicy == RetryPolicy.AutoRetry && isVerified) {
            transferFunds()
        }
    }

    if (retryPolicy == RetryPolicy.ManualRetry || retryPolicy == RetryPolicy.RetryLater) {
        TransactionStatusQuery(
            onClose = {
                retryPolicy = null
            },
            title = "Transaction pending",
            loadingMessage = loadingMessage,
            message = errorMessage,
            onRequery = if (retryPolicy == RetryPolicy.ManualRetry) {
                {
                    activeJob = coroutineScope.launch { transferFunds() }
                }
            } else null,
        )
        return
    }

    if (retryPolicy == null) {
        if (receipt != null && loadingMessage.isBlank()) {
            ReceiptDetails(navController = navController, printJob = receipt!!)
            return
        }

        if (isSameBank == false && bank == null) {
            GetBank(
                title = "Select Bank",
                navController = navController,
                popOnSelect = false,
                onResult = { bank = it }
            )
            return
        }
    }

    Column(
        modifier = Modifier
            .background(colorResource(R.color.menuBackground))
            .fillMaxSize(),
    ) {
        CreditClubAppBar(
            title = title,
            onBackPressed = {
                if (isSameBank == null || receipt != null) {
                    navController.popBackStack()
                } else {
                    isSameBank = null
                    bank = null
                    nameEnquiryResponse = null
                    showConfirmation = false
                    receiverAccountNumber = ""
                    narration = ""
                    amountString = ""
                    loadingMessage = ""
                }
            },
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            if (loadingMessage.isNotBlank()) {
                item(key = "loading") {
                    Loading(
                        message = loadingMessage,
                        onCancel = {
                            activeJob?.cancel()
                            activeJob = null
                            loadingMessage = ""
                        },
                    )
                }
                return@LazyColumn
            }
            if (isSameBank == null && !showConfirmation) {
                item(key = "select-type") {
                    Row(
                        modifier = Modifier
                            .padding(top = 10.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                    ) {
                        SmallMenuButton(
                            text = stringResource(R.string.funds_transfer_same_bank),
                            icon = R.drawable.funds_transfer_same_bank,
                            onClick = { isSameBank = true },
                            draw = true,
                        )
                        SmallMenuButton(
                            text = stringResource(R.string.other_bank),
                            icon = R.drawable.ic_bank_building,
                            onClick = { isSameBank = false },
                        )
                    }
                }
            }
            if (bank != null && !showConfirmation) {
                item(key = "bank") {
                    OutlinedTextField(
                        label = { Text(text = "Bank") },
                        value = bank!!.name ?: bank!!.shortName ?: "",
                        enabled = false,
                        onValueChange = { },
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }
            if (isSameBank != null && !showConfirmation) {
                item(key = "account-number") {
                    OutlinedTextField(
                        label = { Text(text = "Recipient Account Number") },
                        value = receiverAccountNumber,
                        enabled = !isVerified,
                        onValueChange = {
                            val maxLength = institutionConfig.bankAccountNumberLength
                            if (it.length <= maxLength) {
                                receiverAccountNumber = it
                            }
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (accountNumberIsValid && !isVerified) {
                                IconButton(
                                    onClick = {
                                        activeJob = coroutineScope.launch { validateAccount() }
                                    },
                                ) {
                                    Icon(
                                        Icons.Outlined.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colors.onSurface.copy(0.52f)
                                    )
                                }
                            }
                        },
                    )
                }
                if (isVerified && !showConfirmation) {
                    item(key = "account-name") {
                        Text(
                            nameEnquiryResponse!!.beneficiaryAccountName ?: "",
                            modifier = Modifier
                                .padding(top = 4.dp, start = 16.dp, end = 16.dp)
                                .fillMaxWidth(),
                            fontSize = 18.sp,
                        )
                    }
                }
            }
            if (nameEnquiryResponse != null && !showConfirmation) {
                item(key = "amount") {
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
                    )
                }
                item(key = "narration") {
                    OutlinedTextField(
                        value = narration,
                        onValueChange = { narration = it },
                        label = { Text(text = "Narration") },
                        maxLines = 1,
                        singleLine = true,
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                    )
                }
            }

            if (showConfirmation) {
                item(key = "confirm-beneficiary") {
                    DataItem(
                        label = "Beneficiary",
                        value = "${nameEnquiryResponse!!.beneficiaryAccountName}, $receiverAccountNumber"
                    )
                }
                item(key = "confirm-bank") {
                    DataItem(
                        label = "Beneficiary Bank",
                        value = if (isSameBank == true) stringResource(R.string.funds_transfer_same_bank) else bank!!.shortName
                            ?: ""
                    )
                }
                item(key = "confirm-amount") {
                    DataItem(label = "Amount", value = formattedAmount)
                }
                item(key = "narration") {
                    DataItem(label = "Narration", value = narration)
                }
            }

            item(key = "error") {
                ErrorMessage(errorMessage)
            }
        }
        if (loadingMessage.isBlank() && accountNumberIsValid && amountIsValid && receipt == null) {
            AppButton(
                onClick = {
                    activeJob = coroutineScope.launch {
                        when {
                            !showConfirmation -> showConfirmation = true
                            isVerified -> transferFunds()
                            else -> validateAccount()
                        }
                    }
                }
            ) {
                Text(
                    text = when {
                        showConfirmation -> stringResource(R.string.confirm)
                        isVerified -> stringResource(R.string.make_transfer)
                        else -> stringResource(R.string.validate_account)
                    }
                )
            }
        }
    }
}

@Composable
private fun RowScope.SmallMenuButton(
    text: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    draw: Boolean = false,
    tint: Color = colorResource(R.color.colorAccent),
) {
    val context = LocalContext.current
    val drawable = remember(icon) { AppCompatResources.getDrawable(context, icon) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .heightIn(100.dp, 150.dp)
            .weight(1f)
            .padding(8.dp),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .weight(1f),
            elevation = 2.dp,
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.clickable(onClick = onClick),
            ) {
                if (draw) {
                    Box(
                        modifier = Modifier
                            .padding(30.dp)
                            .requiredSize(45.dp)
                            .drawBehind {
                                drawIntoCanvas { canvas ->
                                    drawable?.let {
                                        it.setBounds(
                                            0,
                                            0,
                                            size.width.roundToInt(),
                                            size.height.roundToInt()
                                        )
                                        it.draw(canvas.nativeCanvas)
                                    }
                                }
                            }
                    )
                } else {
                    Image(
                        painterResource(id = icon),
                        contentDescription = null,
                        alignment = Alignment.Center,
                        modifier = Modifier
                            .padding(30.dp)
                            .size(35.dp),
                        colorFilter = if (tint.alpha == 0f) null else ColorFilter.tint(tint),
                    )
                }
            }
        }

        Text(
            text = text.uppercase(Locale.ROOT),
            style = MaterialTheme.typography.button,
            color = colorResource(R.color.menuButtonTextColor),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
