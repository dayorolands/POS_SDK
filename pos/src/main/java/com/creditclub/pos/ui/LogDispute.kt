package com.creditclub.pos.ui


import android.content.ComponentCallbacks
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import com.appzonegroup.creditclub.pos.models.DisputedPosTransaction
import com.appzonegroup.creditclub.pos.models.PosTransaction
import com.appzonegroup.creditclub.pos.models.from
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.model.IntValueType
import com.creditclub.core.ui.widget.DialogProvider
import com.creditclub.pos.api.ChargeBackService
import com.creditclub.ui.CreditClubAppBar
import com.creditclub.ui.Select
import kotlinx.coroutines.launch

suspend fun ChargeBackService.logDispute(
    dialogProvider: DialogProvider,
    request: DisputedPosTransaction
): Boolean {
    val agentPin = dialogProvider.getPin("Agent PIN") ?: return false
    logDispute(agentPin, request)
    return true
}

val issuingBanks = listOf(IntValueType(0, "Polaris Bank"))

@Composable
fun ComponentCallbacks.LogDispute(
    navController: NavHostController,
    chargeBackService: ChargeBackService,
    localStorage: LocalStorage,
    dialogProvider: DialogProvider,
) {
    val (firstName, setFirstName) = remember { mutableStateOf("") }
    val (lastName, setLastName) = remember { mutableStateOf("") }
    val (phoneNumber, setPhoneNumber) = remember { mutableStateOf("") }
    val (email, setEmail) = remember { mutableStateOf("") }
    val (selectedTransaction, setSelectedTransaction) = remember {
        mutableStateOf<PosTransaction?>(null)
    }
    val coroutineScope = rememberCoroutineScope()
    val (issuingBank, setIssuingBank) = remember { mutableStateOf<IntValueType?>(null) }

    if (selectedTransaction == null) {
        GetPosTransaction(navController = navController, popOnSelect = false, onResult = {
            setSelectedTransaction(it)
        })
        return
    }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (appBar, list) = createRefs()

        CreditClubAppBar(
            title = "Log Dispute",
            onBackPressed = { navController.popBackStack() },
            modifier = Modifier.constrainAs(appBar) {
                top.linkTo(parent.top)
                linkTo(
                    start = parent.start,
                    end = parent.end,
                )
            },
        )
        Column(
            modifier = Modifier
                .constrainAs(list) {
                    top.linkTo(appBar.bottom)
                    linkTo(
                        start = parent.start,
                        end = parent.end,
                    )
                }
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Select(
                title = "Issuing Bank",
                options = issuingBanks,
                selected = issuingBank,
                onChange = setIssuingBank,
            )
            OutlinedTextField(
                value = firstName,
                onValueChange = setFirstName,
                label = { Text(text = "Customer First Name") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = setLastName,
                label = { Text(text = "Customer Last Name") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = setPhoneNumber,
                label = { Text(text = "Customer Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
            )
            OutlinedTextField(
                value = email,
                onValueChange = setEmail,
                label = { Text(text = "Customer Email") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
            )
            Button(onClick = {
                coroutineScope.launch {
                    chargeBackService.logDispute(
                        dialogProvider = dialogProvider,
                        request = DisputedPosTransaction.from(selectedTransaction).copy(
//                        issuingBankName = issuingBankName,
                            customerPhoneNumber = phoneNumber,
                            customerFirstName = firstName,
                            customerLastName = lastName,
                            customerEmail = email,
                        )
                    )
                }
            }) {
                Text(text = "Log Dispute")
            }
        }
    }
}
