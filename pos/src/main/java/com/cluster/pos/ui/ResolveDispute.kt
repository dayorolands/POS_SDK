package com.cluster.pos.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavHostController
import com.appzonegroup.creditclub.pos.R
import com.appzonegroup.creditclub.pos.models.DisputedPosTransaction
import com.appzonegroup.creditclub.pos.util.CurrencyFormatter
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.ui.widget.DialogProvider
import com.cluster.core.util.format
import com.cluster.core.util.safeRunIO
import com.cluster.pos.api.ChargeBackService
import com.cluster.ui.CreditClubAppBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ResolveDispute(
    navController: NavHostController,
    chargeBackService: ChargeBackService,
    localStorage: LocalStorage,
    dialogProvider: DialogProvider,
) {
    var disputeToken by remember { mutableStateOf("") }
    val (transaction, setDisputePosTransaction) = remember(disputeToken) {
        mutableStateOf<DisputedPosTransaction?>(null)
    }
    val coroutineScope = rememberCoroutineScope()

    val resolveDispute: suspend CoroutineScope.() -> Unit = remember(disputeToken) {
        resolveDispute@{
            val agentPin = dialogProvider.getPin(R.string.agent_pin) ?: return@resolveDispute
            dialogProvider.showProgressBar(R.string.processing)
            val result = safeRunIO {
                chargeBackService.resolveDispute(
                    localStorage.institutionCode,
                    localStorage.agentPhone,
                    agentPin,
                    disputeToken,
                )
            }
            dialogProvider.hideProgressBar()

            if (result.isFailure) {
                dialogProvider.showErrorAndWait(result.error!!)
                return@resolveDispute
            }
            val response = result.data!!
            if (response.isFailure()) {
                dialogProvider.showErrorAndWait(response.message!!)
                return@resolveDispute
            }
            dialogProvider.showSuccessAndWait(response.message ?: "Your claim has been resolved")
            navController.popBackStack()
        }
    }

    val loadDisputeDetails: suspend CoroutineScope.() -> Unit = remember(disputeToken) {
        loadDisputeDetails@{
            if (disputeToken.isBlank()) {
                dialogProvider.showErrorAndWait("Please enter a valid token")
                return@loadDisputeDetails
            }
            dialogProvider.showProgressBar(R.string.loading)
            val result = safeRunIO {
                chargeBackService.getDisputeDetailsByToken(
                    localStorage.institutionCode,
                    localStorage.agentPhone,
                    disputeToken,
                )
            }
            dialogProvider.hideProgressBar()
            if (result.isFailure) {
                dialogProvider.showErrorAndWait(result.error!!)
                return@loadDisputeDetails
            }

            val response = result.data!!
            if (response.isFailure()) {
                dialogProvider.showErrorAndWait(response.message!!)
                return@loadDisputeDetails
            }
            setDisputePosTransaction(response.data)
        }
    }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (appBar, list) = createRefs()

        CreditClubAppBar(
            title = stringResource(R.string.resolve_dispute),
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
            OutlinedTextField(
                value = disputeToken,
                onValueChange = { disputeToken = it },
                label = { Text(text = stringResource(R.string.dispute_token)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (disputeToken.isNotBlank()) {
                        IconButton(onClick = { coroutineScope.launch { loadDisputeDetails() } }) {
                            Icon(
                                Icons.Outlined.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colors.onSurface.copy(0.52f)
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            )

            if (transaction != null) {
                val amount = remember {
                    CurrencyFormatter.format(transaction.amount.times(100).toString())
                }
                val prettyTime =
                    remember { transaction.transactionDate?.format("MM/dd/uuuu hh:mm:ss") }
                val captionColor = MaterialTheme.colors.onSurface.copy(alpha = 0.52f)
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${transaction.pan}, ${transaction.cardHolder}",
                            style = MaterialTheme.typography.subtitle1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        )
                        Text(
                            text = "$prettyTime \u2022 ${transaction.retrievalReferenceNumber}",
                            style = MaterialTheme.typography.caption,
                            color = captionColor,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = amount,
                            style = MaterialTheme.typography.subtitle1,
                            color = captionColor,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            if (transaction != null) {
                Button(onClick = { coroutineScope.launch { resolveDispute() } }) {
                    Text(text = stringResource(R.string.resolve_dispute))
                }
            }
        }
    }
}
