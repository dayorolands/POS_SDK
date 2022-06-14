package com.cluster.screen.subscription

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cluster.R
import com.cluster.Routes
import com.cluster.core.data.api.SubscriptionService
import com.cluster.core.data.model.SubscriptionMilestone
import com.cluster.core.data.model.SubscriptionRequest
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.type.TransactionType
import com.cluster.core.util.SuspendCallback
import com.cluster.core.util.format
import com.cluster.core.util.safeRunIO
import com.cluster.core.util.toCurrencyFormat
import com.cluster.ui.*
import com.cluster.utility.roundTo2dp
import com.cluster.viewmodel.AppViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun SubscriptionScreen(navController: NavController) {
    val subscriptionService: SubscriptionService by rememberRetrofitService()
    val localStorage: LocalStorage by rememberBean()
    var loadingMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var refreshKey by rememberSaveable { mutableStateOf("") }
    val viewModel: AppViewModel = viewModel()
    val activeSubscription by viewModel.activeSubscription.collectAsState()
    val formattedExpiry = remember(activeSubscription) {
        activeSubscription?.expiryDate?.format("dd/MM/uuuu") ?: ""
    }
    val activePlanId = activeSubscription?.plan?.id ?: 0
    val dialogProvider by rememberDialogProvider()
    val coroutineScope = rememberCoroutineScope()
    val subscriptionMilestones by viewModel.subscriptionMilestones.collectAsState()

    val extendSubscription: SuspendCallback = remember {
        extendSubscription@{
            val subscriptionFeeResult = safeRunIO {
                subscriptionService.getSubscriptionFee(
                    planId = activePlanId,
                    paymentType = 1,
                    institutionCode = localStorage.institutionCode!!,
                    phoneNumber = localStorage.agentPhone!!,
                )
            }
            val feeMessage = "Subscription Fee is ${subscriptionFeeResult.data?.data?.roundTo2dp()}"
            val agentPin = dialogProvider
                .getAgentPin(subtitle = feeMessage) ?: return@extendSubscription
            loadingMessage = "Processing"
            val request = SubscriptionRequest(
                agentPin = agentPin,
                agentPhoneNumber = localStorage.agentPhone!!,
                institutionCode = localStorage.institutionCode!!,
                planId = activePlanId,
                newPlanId = activePlanId,
                autoRenew = true
            )
            val result = safeRunIO {
                subscriptionService.extend(request)
            }
            loadingMessage = ""

            if (result.isFailure) {
                dialogProvider.showError(result.error!!)
                return@extendSubscription
            }

            if (result.data!!.isFailure()) {
                dialogProvider.showError(result.data!!.message!!)
                return@extendSubscription
            }

            kotlinx.coroutines.coroutineScope {
                viewModel.loadSubscriptionData(
                    subscriptionService = subscriptionService,
                    localStorage = localStorage,
                )
            }

            dialogProvider.showSuccess(result.data!!.message!!)
        }
    }

    LaunchedEffect(refreshKey) {
        loadingMessage = "Loading Subscription"
        viewModel.loadSubscriptionData(
            subscriptionService = subscriptionService,
            localStorage = localStorage,
        )
        loadingMessage = ""
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
    ) {
        val (appBar, list) = createRefs()

        CreditClubAppBar(
            title = stringResource(R.string.subscription),
            onBackPressed = { navController.popBackStack() },
            modifier = Modifier.constrainAs(appBar) {
                top.linkTo(parent.top)
                linkTo(
                    start = parent.start,
                    end = parent.end,
                )
            },
            actions = {
                IconButton(onClick = {
                    navController.navigate(Routes.SubscriptionHistory)
                }) {
                    Icon(
                        Icons.Outlined.History,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onPrimary.copy(0.52f)
                    )
                }
            }
        )
        SwipeRefresh(
            state = rememberSwipeRefreshState(loadingMessage.isNotBlank()),
            onRefresh = { refreshKey = UUID.randomUUID().toString() },
            modifier = Modifier.constrainAs(list) {
                top.linkTo(appBar.bottom)
                linkTo(
                    start = parent.start,
                    end = parent.end,
                )
            },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (errorMessage.isNotBlank()) {
                    item {
                        ErrorFeedback(errorMessage = errorMessage)
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 16.dp),
                    ) {
                        if (activeSubscription == null) {
                            ChipButton(
                                label = stringResource(R.string.new_subscription),
                                onClick = {
                                    navController.navigate(Routes.NewSubscription)
                                },
                                imageVector = Icons.Outlined.Add,
                            )
                        } else {
                            Text(
                                text = activeSubscription!!.plan.name,
                                softWrap = true,
                                style = MaterialTheme.typography.h4,
                                color = MaterialTheme.colors.onSurface,
                            )
                            Text(
                                text = "Expires $formattedExpiry",
                                softWrap = true,
                                style = MaterialTheme.typography.body1,
                                color = MaterialTheme.colors.onSurface,
                            )
                            Spacer(modifier = Modifier.padding(top = 20.dp))
                            ChipButton(
                                label = stringResource(R.string.extend),
                                onClick = {
                                    coroutineScope.launch {
                                        extendSubscription()
                                    }
                                },
                                imageVector = Icons.Outlined.Update
                            )
                            ChipButton(
                                label = stringResource(R.string.upgrade),
                                onClick = {
                                    navController.navigate(Routes.UpgradeSubscription)
                                },
                                imageVector = Icons.Outlined.Upgrade,
                            )
                            ChipButton(
                                label = stringResource(R.string.opt_out_auto_renewal),
                                onClick = {
                                    coroutineScope.launch {
                                        extendSubscription()
                                    }
                                },
                                imageVector = Icons.Outlined.Cancel
                            )
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
                item {
                    Text(
                        text = "Milestones",
                        style = MaterialTheme.typography.h6,
                        modifier = Modifier.padding(16.dp),
                    )
                }
                items(subscriptionMilestones, key = { it.id }) {
                    MilestoneItem(item = it)
                }
                item {
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
        }
    }
}

@Composable
private fun ChipButton(label: String, onClick: () -> Unit, imageVector: ImageVector) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .border(
                BorderStroke(
                    1.dp,
                    MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                ),
                RoundedCornerShape(15.dp),
            )
    ) {
        Image(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier
                .padding(8.dp)
                .size(24.dp),
        )
        Text(
            text = label.uppercase(Locale.ROOT),
            color = colorResource(R.color.menuButtonTextColor),
            style = MaterialTheme.typography.caption,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 5.dp),
        )
    }
}

@Composable
private fun MilestoneItem(item: SubscriptionMilestone) {
    val progress = remember(item) {
        1 - (item.targetVolumeLeft / item.targetVolumeMaxLimit).toFloat()
    }
    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    ).value
    val label = remember(item) { TransactionType.find(item.transactionType).label }
    val formattedCurrent = remember(item) {
        (item.targetVolumeMaxLimit - item.targetVolumeLeft).toCurrencyFormat()
    }
    val formattedTarget = remember(item) { item.targetVolumeMaxLimit.toCurrencyFormat() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Text(label, style = MaterialTheme.typography.subtitle1)
        LinearProgressIndicator(progress = animatedProgress)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Current Volume: $formattedCurrent")
            Spacer(modifier = Modifier.weight(1f))
            Text("Target: $formattedTarget")
        }
    }
}