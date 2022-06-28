package com.cluster.screen.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cluster.R
import com.cluster.core.data.api.SubscriptionService
import com.cluster.core.data.model.ChangeSubscriptionRequest
import com.cluster.core.data.model.SubscriptionPlan
import com.cluster.core.data.model.SubscriptionRequest
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.util.getMessage
import com.cluster.core.util.safeRunIO
import com.cluster.core.util.toCurrencyFormat
import com.cluster.ui.*
import com.cluster.utility.roundTo2dp
import com.cluster.viewmodel.AppViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChooseSubscriptionScreen(
    navController: NavController,
    isUpgrade: Boolean,
    isChangeSubscription: Boolean
) {
    val context = LocalContext.current
    val subscriptionService: SubscriptionService by rememberRetrofitService()
    val localStorage: LocalStorage by rememberBean()
    var loadingMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val viewModel: AppViewModel = viewModel()
    var refreshKey by remember { mutableStateOf(UUID.randomUUID().toString()) }
    val dialogProvider by rememberDialogProvider()
    var selectedPlan: SubscriptionPlan? by remember { mutableStateOf(null) }
    val activePlanId = selectedPlan?.id
    var isRefreshing by remember { mutableStateOf(false) }
    var changePlan by remember { mutableStateOf(false)}

    val subscriptionPlans by produceState(emptyList(), refreshKey) {
        value = viewModel.subscriptionPlans.value
        isRefreshing = true

        if(isChangeSubscription) changePlan = true
        val subscriptionPlansResult = safeRunIO {
            subscriptionService.getSubscriptionPlans(
                institutionCode = localStorage.institutionCode!!,
                agentPhoneNumber = localStorage.agentPhone!!,
                changePlan = changePlan
            )
        }
        isRefreshing = false
        if (subscriptionPlansResult.isFailure) {
            errorMessage = subscriptionPlansResult.error!!.getMessage(context)
            return@produceState
        }
        errorMessage = ""
        if (subscriptionPlansResult.data?.data == null) return@produceState
        value = subscriptionPlansResult.data!!.data!!
        viewModel.subscriptionPlans.value = value
    }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(BottomSheetValue.Collapsed)
    )
    val coroutineScope = rememberCoroutineScope()

    val chooseSubscription: suspend (plan: SubscriptionPlan, agentPin: String, autoRenew: Boolean) -> Unit = remember {
        chooseSubscription@{ plan, agentPin, autoRenew ->
            loadingMessage = "Processing"
            val request = SubscriptionRequest(
                agentPin = agentPin,
                agentPhoneNumber = localStorage.agentPhone!!,
                institutionCode = localStorage.institutionCode!!,
                planId = plan.id,
                autoRenew = autoRenew
            )
            val result = safeRunIO {
                if (isUpgrade) {
                    subscriptionService.upgrade(request)
                } else if(isChangeSubscription){
                    subscriptionService.changeSubscriptionPlan(request)
                }
                else {
                    subscriptionService.subscribe(request)
                }
            }
            loadingMessage = ""

            if (result.isFailure) {
                dialogProvider.showError(result.error!!)
                return@chooseSubscription
            }

            if (result.data!!.isFailure()) {
                dialogProvider.showError(result.data!!.message!!)
                return@chooseSubscription
            }

            dialogProvider.showSuccessAndWait(result.data!!.message!!)
            navController.popBackStack()
        }
    }

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            if (selectedPlan != null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                            }
                        },
                        modifier = Modifier.padding(top = 5.dp),
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary.copy(0.52f)
                        )
                    }
                    AppButton(onClick = {
                        coroutineScope.launch {
                            val subscriptionFeeResult = safeRunIO {
                                subscriptionService.getSubscriptionFee(
                                    planId = activePlanId!!,
                                    paymentType = if(isUpgrade) 2 else if (isChangeSubscription) 5 else 0,
                                    institutionCode = localStorage.institutionCode!!,
                                    phoneNumber = localStorage.agentPhone!!,
                                )
                            }
                            val feeMessage = "Subscription Fee is ${subscriptionFeeResult.data?.data?.roundTo2dp()}"
                            bottomSheetScaffoldState.bottomSheetState.collapse()
                            val agentPin = dialogProvider
                                .getAgentPin(subtitle = feeMessage) ?: return@launch
                                chooseSubscription(selectedPlan!!, agentPin, false)

                        }
                    }) {
                        Text("Confirm ${selectedPlan!!.name} plan?")
                    }
                }
            }
        },
        sheetShape = RoundedCornerShape(topEnd = 20.dp, topStart = 20.dp),
        backgroundColor = MaterialTheme.colors.surface,
        sheetPeekHeight = 0.dp,
        topBar = {
            CreditClubAppBar(
                title = if (isUpgrade) stringResource(R.string.upgrade) else if (isChangeSubscription) stringResource(R.string.change) else stringResource(R.string.choose_a_plan),
                onBackPressed = { navController.popBackStack() },
            )
        }
    ) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { refreshKey = UUID.randomUUID().toString() },
            swipeEnabled = loadingMessage.isNotBlank(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (loadingMessage.isNotBlank()) {
                    item {
                        Loading(loadingMessage)
                    }
                    return@LazyColumn
                }

                if (errorMessage.isNotBlank()) {
                    item {
                        ErrorFeedback(errorMessage = errorMessage)
                    }
                }

                items(subscriptionPlans, key = { it.id }) {
                    SubscriptionPlanItem(
                        item = it,
                        onClick = {
                            selectedPlan = it
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            }
                        },
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
        }
    }
}

@Composable
private inline fun SubscriptionPlanItem(
    item: SubscriptionPlan,
    noinline onClick: () -> Unit) {
    val formattedFee = remember { item.fee.toCurrencyFormat() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 10.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                item.name,
                color = colorResource(id = R.color.colorPrimary),
                maxLines = 1,
                style = MaterialTheme.typography.subtitle1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 0.dp),
            )
            Text(
                text = formattedFee,
                maxLines = 1,
                style = MaterialTheme.typography.caption,
                color = colorResource(R.color.ef_grey),
            )
        }
        Text(
            text = item.description,
            maxLines = 1,
            style = MaterialTheme.typography.body1,
            color = colorResource(R.color.ef_grey),
            modifier = Modifier
                .padding(start = 16.dp, bottom = 10.dp, end = 16.dp)
                .fillMaxWidth(),
        )
        Divider(startIndent = 16.dp)
    }
}

@Preview
@Composable
private fun SubscriptionPlanItemPreview() {
    val dateCreated = Instant.now()
    LazyColumn {
        items(5) {
            val subscriptionPlan = SubscriptionPlan(
                id = it,
                dateCreated = dateCreated,
                dateUpdated = dateCreated,
                name = "New note $it",
                description = "Very new note",
                displayMessage = "07239488",
                isActive = true,
                fee = 500.0,
                validityPeriod = 30,
                institutionCode = "100305",
            )
            SubscriptionPlanItem(item = subscriptionPlan, onClick = {})
        }
    }
}