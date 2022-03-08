package com.cluster.screen.subscription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.outlined.Upgrade
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.cluster.core.data.model.SubscriptionRequest
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.util.SuspendCallback
import com.cluster.core.util.format
import com.cluster.core.util.safeRunIO
import com.cluster.ui.*
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

    val extendSubscription: SuspendCallback = remember {
        extendSubscription@{
            val agentPin = dialogProvider.getAgentPin() ?: return@extendSubscription
            loadingMessage = "Processing"
            val request = SubscriptionRequest(
                agentPin = agentPin,
                agentPhoneNumber = localStorage.agentPhone!!,
                institutionCode = localStorage.institutionCode!!,
                planId = activePlanId,
                newPlanId = activePlanId,
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
                viewModel.loadActiveSubscription(
                    subscriptionService = subscriptionService,
                    localStorage = localStorage,
                )
            }

            dialogProvider.showSuccess(result.data!!.message!!)
        }
    }

    LaunchedEffect(refreshKey) {
        viewModel.loadActiveSubscription(
            subscriptionService = subscriptionService,
            localStorage = localStorage,
        )
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
    ) {
        val (appBar, list, fab) = createRefs()

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
                                imageVector = Icons.Outlined.Add,
                            ) {
                                navController.navigate(Routes.NewSubscription)
                            }
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
                                imageVector = Icons.Outlined.Update
                            ) {
                                coroutineScope.launch {
                                    extendSubscription()
                                }
                            }
                            ChipButton(
                                label = stringResource(R.string.upgrade),
                                imageVector = Icons.Outlined.Upgrade,
                            ) {
                                navController.navigate(Routes.UpgradeSubscription)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
        }
    }
}

@Composable
private fun ChipButton(label: String, imageVector: ImageVector, onClick: () -> Unit) {
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