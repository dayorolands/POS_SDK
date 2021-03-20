package com.creditclub.pos.ui


import android.content.ComponentCallbacks
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.appzonegroup.creditclub.pos.R
import com.appzonegroup.creditclub.pos.models.DisputedPosTransaction
import com.appzonegroup.creditclub.pos.models.PosTransaction
import com.appzonegroup.creditclub.pos.models.from
import com.creditclub.core.data.model.Bank
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.core.util.includesSpecialCharacters
import com.creditclub.core.util.isValidEmail
import com.creditclub.core.util.safeRunIO
import com.creditclub.pos.api.ChargeBackService
import com.creditclub.ui.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ComponentCallbacks.LogDispute(
    navController: NavHostController,
    dialogProvider: DialogProvider,
) {
    val chargeBackService: ChargeBackService by rememberRetrofitService()
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var phoneNumber by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    val (selectedTransaction, setSelectedTransaction) = remember {
        mutableStateOf<PosTransaction?>(null)
    }
    val coroutineScope = rememberCoroutineScope()
    val (issuingBank, setIssuingBank) = remember { mutableStateOf<Bank?>(null) }
    var loadingMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val logDispute: suspend CoroutineScope.() -> Unit =
        remember(selectedTransaction, phoneNumber, firstName, lastName, email, issuingBank) {
            logDispute@{
                if (issuingBank == null) {
                    errorMessage = "Issuing bank is required"
                    return@logDispute
                }
                if (firstName.isBlank() || firstName.includesSpecialCharacters()) {
                    errorMessage = "Please enter a valid first name"
                    return@logDispute
                }
                if (lastName.isBlank() || lastName.includesSpecialCharacters()) {
                    errorMessage = "Please enter a valid last name"
                    return@logDispute
                }
                if (phoneNumber.length != 11) {
                    errorMessage = "Please enter a valid phone number"
                    return@logDispute
                }
                if (email.isBlank() || !email.isValidEmail()) {
                    errorMessage = "Please enter a valid email"
                    return@logDispute
                }

                errorMessage = ""
                if (selectedTransaction == null) return@logDispute
                val agentPin = dialogProvider.getPin(R.string.agent_pin) ?: return@logDispute

                loadingMessage = "Processing Request"
                val (response, error) = safeRunIO {
                    chargeBackService.logDispute(
                        agentPin = agentPin,
                        request = DisputedPosTransaction.from(selectedTransaction).copy(
                            issuingBankName = issuingBank.name,
                            customerPhoneNumber = phoneNumber,
                            customerFirstName = firstName,
                            customerLastName = lastName,
                            customerEmail = email,
                        )
                    )
                }
                loadingMessage = ""
                if (error != null) {
                    dialogProvider.showErrorAndWait(error)
                    return@logDispute
                }
                if (response!!.isFailure()) {
                    dialogProvider.showErrorAndWait(response.message!!)
                    return@logDispute
                }
                dialogProvider.showSuccessAndWait(
                    response.message ?: "Your dispute has been logged."
                )
                navController.popBackStack()
            }
        }

    if (selectedTransaction == null) {
        GetPosTransaction(
            navController = navController,
            popOnSelect = false,
            onResult = setSelectedTransaction
        )
        return
    }

    if (issuingBank == null) {
        GetBank(
            title = "Select Issuing Bank",
            navController = navController,
            popOnSelect = false,
            onResult = setIssuingBank
        )
        return
    }

    Column {
        CreditClubAppBar(
            title = stringResource(R.string.log_dispute),
            onBackPressed = { navController.popBackStack() },
        )
        LazyColumn(modifier = Modifier.weight(1f)) {
            if (loadingMessage.isNotBlank()) {
                item {
                    Loading(message = loadingMessage)
                }
                return@LazyColumn
            }
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        label = { Text(text = "Issuing Bank") },
                        value = issuingBank.name ?: "",
                        onValueChange = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text(text = "Customer First Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text(text = "Customer Last Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text(text = "Customer Phone Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(text = "Customer Email") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                    )
                }
            }
            if (errorMessage.isNotBlank()) {
                item {
                    ErrorMessage(errorMessage)
                }
            }
        }
        if (loadingMessage.isBlank()) {
            Button(
                modifier = Modifier
                    .padding(16.dp)
                    .height(45.dp)
                    .width(130.dp),
                onClick = { coroutineScope.launch { logDispute() } }) {
                Text(text = stringResource(R.string.log_dispute))
            }
        }
    }
}
