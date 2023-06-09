package com.cluster.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cluster.R
import com.cluster.core.data.api.AuthService
import com.cluster.core.data.model.TransactionPinChangeRequest
import com.cluster.core.data.prefs.AppDataStorage
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.util.SuspendCallback
import com.cluster.core.util.safeRunIO
import com.cluster.ui.*
import com.cluster.utility.FunctionIds
import com.cluster.utility.FunctionUsageTracker
import kotlinx.coroutines.launch

@Composable
fun PinChange(navController: NavController) {
    FunctionUsageTracker(FunctionIds.AGENT_CHANGE_PIN)

    val dialogProvider by rememberDialogProvider()
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmNewPin by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var loadingMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val authService: AuthService by rememberRetrofitService()
    val localStorage: LocalStorage by rememberBean()
    val appDataStorage: AppDataStorage by rememberBean()

    val changePin: SuspendCallback =
        remember(confirmNewPin, oldPin, newPin) {
            changePin@{
                if (oldPin.isEmpty()) {
                    dialogProvider.showError("Please enter the customer's old PIN")
                    return@changePin
                }
                if (oldPin.length != 4) {
                    dialogProvider.showError("Please enter the complete PIN")
                    return@changePin
                }
                if (newPin.isEmpty()) {
                    dialogProvider.showError("Please enter your PIN")
                    return@changePin
                }
                if (newPin.length != 4) {
                    dialogProvider.showError("Please enter the complete new PIN")
                    return@changePin
                }
                if (confirmNewPin != newPin) {
                    dialogProvider.showError(context.getString(R.string.new_pin_confirmation_mismatch))
                    return@changePin
                }

                errorMessage = ""
                loadingMessage = "Processing Request"
                val (response, error) = safeRunIO {
                    authService.changeTransactionPin(
                        request = TransactionPinChangeRequest(
                            agentPhoneNumber = localStorage.agentPhone!!,
                            institutionCode = localStorage.institutionCode!!,
                            newPin = newPin,
                            confirmNewPin = confirmNewPin,
                            oldPin = oldPin,
                            geoLocation = localStorage.lastKnownLocation,
                            deviceId = appDataStorage.deviceId!!,
                        )
                    )
                }
                loadingMessage = ""
                if (error != null) {
                    dialogProvider.showErrorAndWait(error)
                    return@changePin
                }
                if (response!!.isFailure()) {
                    dialogProvider.showErrorAndWait(response.responseMessage!!)
                    return@changePin
                }
                dialogProvider.showSuccessAndWait(
                    response.responseMessage ?: "Your pin has been changed."
                )
                navController.popBackStack()
            }
        }

    Column {
        CreditClubAppBar(
            title = stringResource(R.string.change_pin),
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
                        value = oldPin,
                        onValueChange = { if (it.length <= 4) oldPin = it },
                        label = { Text(text = "Old PIN") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 4) newPin = it },
                        label = { Text(text = "New PIN") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    OutlinedTextField(
                        value = confirmNewPin,
                        onValueChange = { if (it.length <= 4) confirmNewPin = it },
                        label = { Text(text = "Confirm New PIN") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
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
                onClick = { coroutineScope.launch { changePin() } }) {
                Text(text = stringResource(R.string.change_pin))
            }
        }
    }
}