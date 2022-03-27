package com.cluster.screen.home

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.cluster.*
import com.cluster.R
import com.cluster.activity.UpdateActivity
import com.cluster.components.NavigationRow
import com.cluster.core.config.InstitutionConfig
import com.cluster.core.data.api.SubscriptionService
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.util.debugOnly
import com.cluster.core.util.packageInfo
import com.cluster.pos.Platform
import com.cluster.ui.rememberBean
import com.cluster.ui.rememberRetrofitService
import com.cluster.utility.logout
import com.cluster.utility.openPageById
import com.cluster.viewmodel.AppViewModel
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    composeNavController: NavHostController,
    fragment: CreditClubFragment,
) {
    val viewModel: AppViewModel = viewModel()
    val localStorage: LocalStorage by rememberBean()
    val institutionConfig: InstitutionConfig by rememberBean()
    val agent = localStorage.agent
    val context = LocalContext.current
    val subscriptionService: SubscriptionService by rememberRetrofitService()

    LaunchedEffect(1) {
        viewModel.loadActiveSubscription(
            subscriptionService = subscriptionService,
            localStorage = localStorage,
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        item {
            Text(
                text = agent?.agentName ?: "",
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxSize(),
                softWrap = true,
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
            )
        }

        item {
            Text(
                text = agent?.phoneNumber ?: "",
                modifier = Modifier
                    .padding(bottom = 5.dp)
                    .fillMaxSize(),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface.copy(0.5f),
                textAlign = TextAlign.Center,
            )
        }

        item {
            ActiveSubscription(
                openSubscription = { composeNavController.navigate(Routes.Subscription) },
            )
        }

        item {
            Row(Modifier.padding(vertical = 10.dp)) {
                ChipButton(
                    label = stringResource(R.string.check_balance),
                    icon = painterResource(R.drawable.income),
                    onClick = { fragment.openPageById(R.id.agent_balance_enquiry_button) }
                )
                ChipButton(
                    label = stringResource(R.string.title_activity_basic_mini_statement),
                    icon = painterResource(R.drawable.deposit),
                    onClick = { fragment.openPageById(R.id.agent_mini_statement_button) }
                )
            }
        }

        if (institutionConfig.hasOnlineFunctions) {
            item {
                NavigationRow(
                    title = stringResource(R.string.online_functions),
                    imageVector = Icons.Outlined.Language,
                    onClick = {
                        context.startActivity(Intent(context, OnlineActivity::class.java))
                    }
                )
            }
        }

        item {
            NavigationRow(
                title = stringResource(R.string.reports),
                imageVector = Icons.Outlined.ReceiptLong,
                onClick = {
                    context.startActivity(Intent(context, ReportActivity::class.java))
                }
            )
        }

        item {
            NavigationRow(
                title = stringResource(R.string.commission),
                imageVector = Icons.Outlined.Payments,
                onClick = {
                    context.startActivity(Intent(context, CommissionsActivity::class.java))
                }
            )
        }

        item {
            NavigationRow(
                title = stringResource(R.string.pending_transactions),
                imageVector = Icons.Outlined.HourglassEmpty,
                onClick = {
                    composeNavController.navigate(Routes.PendingTransactions)
                }
            )
        }

        item {
            NavigationRow(
                title = stringResource(R.string.support),
                imageVector = Icons.Outlined.ChatBubbleOutline,
                onClick = {
                    composeNavController.navigate(Routes.SupportCases)
                }
            )
        }

        if (institutionConfig.hasHlaTagging) {
            item {
                NavigationRow(
                    title = stringResource(R.string.hla_tagging),
                    imageVector = Icons.Outlined.Place,
                    onClick = {
                        context.startActivity(Intent(context, HlaTaggingActivity::class.java))
                    }
                )
            }
        }

        item {
            NavigationRow(
                title = stringResource(R.string.change_transaction_pin),
                imageVector = Icons.Outlined.Lock,
                onClick = {
                    composeNavController.navigate(Routes.PinChange)
                }
            )
        }

        item {
            NavigationRow(
                title = stringResource(R.string.change_password),
                imageVector = Icons.Outlined.Lock,
                onClick = {
                    composeNavController.navigate(Routes.ChangePassword)
                }
            )
        }

        item {
            NavigationRow(
                title = stringResource(R.string.title_activity_faq),
                imageVector = Icons.Outlined.HelpOutline,
                onClick = {
                    context.startActivity(Intent(context, FaqActivity::class.java))
                }
            )
        }

        if (Platform.isPOS && Platform.deviceType != 2) {
            item {
                NavigationRow(
                    title = stringResource(R.string.update),
                    imageVector = Icons.Outlined.ArrowDownward,
                    onClick = {
                        context.startActivity(Intent(context, UpdateActivity::class.java))
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }

        debugOnly {
            item {
                Text(
                    text = "For testing purposes only",
                    modifier = Modifier.padding(start = 16.dp, bottom = 5.dp),
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(0.5f),
                )
            }
        }

        item {
            Text(
                text = "v${context.packageInfo?.versionName}. Powered by Cluster",
                modifier = Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onSurface.copy(0.5f),
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { (context as Activity).logout() },
            ) {
                Text(
                    stringResource(R.string.logout),
                    style = MaterialTheme.typography.h5,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun ChipButton(label: String, icon: Painter, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
            .widthIn(max = 300.dp, min = 80.dp)
            .padding(end = 10.dp)
            .border(
                BorderStroke(
                    1.dp,
                    MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                ),
                RoundedCornerShape(15.dp),
            )
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
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