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
import com.cluster.core.data.model.PasswordChangeRequest
import com.cluster.core.data.prefs.AppDataStorage
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.util.SuspendCallback
import com.cluster.core.util.safeRunIO
import com.cluster.ui.*
import com.cluster.utility.FunctionIds
import com.cluster.utility.FunctionUsageTracker
import kotlinx.coroutines.launch

private const val PASSWORD_LENGTH = 6

@Composable
fun ChangePasswordScreen(navController: NavController) {
    FunctionUsageTracker(FunctionIds.CHANGE_PASSWORD)

    val dialogProvider by rememberDialogProvider()
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    var loadingMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val authService: AuthService by rememberRetrofitService()
    val localStorage: LocalStorage by rememberBean()
    val appDataStorage: AppDataStorage by rememberBean()

    val changePassword: SuspendCallback =
        remember(confirmNewPassword, oldPassword, newPassword) {
            changePassword@{
                if (oldPassword.isEmpty()) {
                    dialogProvider.showError("Please enter your password")
                    return@changePassword
                }
                if (oldPassword.length != PASSWORD_LENGTH) {
                    dialogProvider.showError("Please enter a ${PASSWORD_LENGTH}-digit password")
                    return@changePassword
                }
                if (newPassword.isEmpty()) {
                    dialogProvider.showError("Please enter your Password")
                    return@changePassword
                }
                if (newPassword.length != PASSWORD_LENGTH) {
                    dialogProvider.showError("New password must be a ${PASSWORD_LENGTH}-digit number")
                    return@changePassword
                }
                if (confirmNewPassword != newPassword) {
                    dialogProvider.showError(context.getString(R.string.new_password_confirmation_mismatch))
                    return@changePassword
                }

                errorMessage = ""
                loadingMessage = "Processing Request"
                val (response, error) = safeRunIO {
                    authService.changePassword(
                        request = PasswordChangeRequest(
                            agentPhoneNumber = localStorage.agentPhone!!,
                            institutionCode = localStorage.institutionCode!!,
                            password = newPassword,
                            confirmPassword = confirmNewPassword,
                            oldPassword = oldPassword,
                            geoLocation = localStorage.lastKnownLocation,
                            deviceId = appDataStorage.deviceId!!,
                        )
                    )
                }
                loadingMessage = ""
                if (error != null) {
                    dialogProvider.showErrorAndWait(error)
                    return@changePassword
                }
                if (response!!.isFailure()) {
                    dialogProvider.showErrorAndWait(response.responseMessage!!)
                    return@changePassword
                }
                dialogProvider.showSuccessAndWait(
                    response.responseMessage ?: "Your password has been changed."
                )
                navController.popBackStack()
            }
        }

    Column {
        CreditClubAppBar(
            title = stringResource(R.string.change_password),
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
                        value = oldPassword,
                        onValueChange = { if (it.length <= PASSWORD_LENGTH) oldPassword = it },
                        label = { Text(text = "Old Password") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { if (it.length <= PASSWORD_LENGTH) newPassword = it },
                        label = { Text(text = "New Password") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                    )
                    OutlinedTextField(
                        value = confirmNewPassword,
                        onValueChange = {
                            if (it.length <= PASSWORD_LENGTH) confirmNewPassword = it
                        },
                        label = { Text(text = "Confirm New Password") },
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
                    .width(160.dp),
                onClick = { coroutineScope.launch { changePassword() } },
            ) {
                Text(text = stringResource(R.string.change_password))
            }
        }
    }
}