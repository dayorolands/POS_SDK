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
import androidx.compose.ui.graphics.painter.Painter
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
import com.appzonegroup.app.fasttrack.receipt.FundsTransferReceipt
import com.creditclub.core.config.IInstitutionConfig
import com.creditclub.core.data.api.FundsTransferService
import com.creditclub.core.data.model.Bank
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.data.request.FundsTransferRequest
import com.creditclub.core.data.response.NameEnquiryResponse
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.*
import com.creditclub.pos.printer.PrintJob
import com.creditclub.ui.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import java.time.Instant
import java.util.*
import kotlin.math.roundToInt

@Composable
fun FundsTransfer(navController: NavController, dialogProvider: DialogProvider) {
    val context = LocalContext.current
    val fundsTransferService: FundsTransferService by rememberRetrofitService()
    val localStorage: LocalStorage by rememberBean()
    val institutionConfig: IInstitutionConfig by rememberBean()
    val gps: TrackGPS by rememberBean()
    val coroutineScope = rememberCoroutineScope()
    var loadingMessage by remember { mutableStateOf("") }
    var isSameBank: Boolean? by remember { mutableStateOf(null) }
    var bank: Bank? by remember(isSameBank) { mutableStateOf(null) }
    var receiverAccountNumber by remember(bank) { mutableStateOf("") }
    var amountString by remember(bank) { mutableStateOf("") }
    var nameEnquiryResponse: NameEnquiryResponse? by remember(bank, receiverAccountNumber) {
        mutableStateOf(null)
    }
    val isVerified = nameEnquiryResponse != null
    var narration by remember { mutableStateOf("") }
    var receipt: PrintJob? by remember { mutableStateOf(null) }
    val transactionReference = rememberSaveable { UUID.randomUUID().toString().substring(0, 8) }
    val accountNumberIsValid = remember(receiverAccountNumber) {
        receiverAccountNumber.isNotBlank() && (receiverAccountNumber.length == 10 || receiverAccountNumber.length == 11)
    }
    val amountIsValid = remember(amountString) {
        with(amountString.toDoubleOrNull()) {
            this != null && this > 0.0
        }
    }
    val formattedAmount = remember(amountString, amountIsValid) {
        if (amountIsValid) amountString.toDouble().toCurrencyFormat() else ""
    }
    var errorMessage by remember(receiverAccountNumber, amountString) {
        mutableStateOf("")
    }
    var showConfirmation by remember(nameEnquiryResponse, isSameBank) { mutableStateOf(false) }
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
                    agentPhoneNumber = localStorage.agentPhone,
                    institutionCode = localStorage.institutionCode,
                    authToken = "95C1D8B4-7589-4F70-8F20-473E89FB5F01",
                    isToRelatedCommercialBank = isSameBank == true,
                    externalTransactionReference = transactionReference,
                    geoLocation = gps.geolocationString,
                    beneficiaryAccountNumber = receiverAccountNumber,
                    beneficiaryInstitutionCode = bank?.code,
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
        ) {
            makeTransfer@{
                errorMessage = ""
                val pin = dialogProvider.getPin("Agent PIN") ?: return@makeTransfer
                if (pin.isEmpty()) return@makeTransfer dialogProvider.showError("Please enter your PIN")
                if (pin.length != 4) return@makeTransfer dialogProvider.showError("PIN must be four digits")

                val fundsTransferRequest = FundsTransferRequest(
                    agentPhoneNumber = localStorage.agentPhone,
                    institutionCode = localStorage.institutionCode,
                    agentPin = pin,
                    authToken = "95C1D8B4-7589-4F70-8F20-473E89FB5F01",
                    beneficiaryAccountNumber = receiverAccountNumber,
                    amountInNaira = amountString.toDouble(),
                    isToRelatedCommercialBank = isSameBank ?: false,
                    externalTransactionReference = transactionReference,
                    geoLocation = gps.geolocationString,
                    narration = narration.trim { it <= ' ' },
                    beneficiaryInstitutionCode = bank?.code,
                    beneficiaryAccountName = nameEnquiryResponse?.beneficiaryAccountName,
                    beneficiaryBVN = nameEnquiryResponse?.beneficiaryBVN,
                    beneficiaryKYC = nameEnquiryResponse?.beneficiaryKYC,
                    nameEnquirySessionID = nameEnquiryResponse?.nameEnquirySessionID,
                )

                loadingMessage = "Transfer in progress"
                val (response, error) = safeRunIO {
                    fundsTransferService.transfer(fundsTransferRequest)
                }
                loadingMessage = ""

                if (error != null) {
                    dialogProvider.showErrorAndWait(error)
                    navController.popBackStack()
                    return@makeTransfer
                }
                if (response == null) {
                    dialogProvider.showErrorAndWait(context.getString(R.string.network_error_message))
                    navController.popBackStack()
                    return@makeTransfer
                }

                receipt = FundsTransferReceipt(
                    context,
                    fundsTransferRequest,
                    Instant.now().toString("dd-MM-yyyy hh:mm"),
                    isSuccessful = response.isSuccessful,
                    reason = response.responseMessage,
                )
            }
        }

    LaunchedEffect(receiverAccountNumber) {
        if (receiverAccountNumber.length == institutionConfig.bankAccountNumberLength
            && loadingMessage.isBlank()
            && nameEnquiryResponse == null
        ) {
            validateAccount()
        }
    }

    if (receipt != null) {
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
    Column(
        modifier = Modifier
            .background(colorResource(R.color.menuBackground))
            .fillMaxSize(),
    ) {
        CreditClubAppBar(
            title = when {
                isSameBank == null -> stringResource(R.string.funds_transfer)
                !isVerified -> stringResource(R.string.enter_account)
                showConfirmation -> stringResource(R.string.confirm)
                else -> stringResource(R.string.funds_transfer)
            },
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
                    Loading(message = loadingMessage)
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
                                IconButton(onClick = { coroutineScope.launch { validateAccount() } }) {
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
                    coroutineScope.launch {
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
    tint: Color = colorResource(R.color.colorAccent)
) {
    val context = LocalContext.current
    val drawable = AppCompatResources.getDrawable(context, icon)
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
                .weight(1f)
                .clickable(onClick = onClick),
            elevation = 2.dp,
            shape = RoundedCornerShape(20.dp),
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

        Text(
            text = text.toUpperCase(Locale.ROOT),
            style = MaterialTheme.typography.button,
            color = colorResource(R.color.menuButtonTextColor),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun DataItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .fillMaxWidth()
            )
            Text(
                text = value,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        Divider()
    }
}