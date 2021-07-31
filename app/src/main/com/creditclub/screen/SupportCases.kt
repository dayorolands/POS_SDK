package com.creditclub.screen

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.appzonegroup.app.fasttrack.CaseLogActivity
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.utility.FunctionUsageTracker
import com.appzonegroup.app.fasttrack.utility.FunctionIds
import com.creditclub.Routes
import com.creditclub.core.data.api.CaseLogService
import com.creditclub.core.data.model.CaseDetail
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.data.request.CaseDetailsRequest
import com.creditclub.core.util.*
import com.creditclub.ui.*
import com.creditclub.viewmodel.AppViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.time.LocalDate
import java.util.*

private val localDateNow = LocalDate.now()
private val today = localDateNow.format("dd/MM/uuuu")
private val yesterday = localDateNow.minusDays(1).format("dd/MM/uuuu")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SupportCases(navController: NavController) {
    FunctionUsageTracker(FunctionIds.SUPPORT)

    val context = LocalContext.current
    val caseLogService: CaseLogService by rememberRetrofitService()
    val localStorage: LocalStorage by rememberBean()
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var refreshKey by remember { mutableStateOf("") }
    val viewModel: AppViewModel = viewModel()
    val caseDetails by produceState(viewModel.caseDetails.value, refreshKey) {
        value = viewModel.caseDetails.value
        loading = true
        val (response, error) = safeRunIO {
            caseLogService.caseDetails(
                CaseDetailsRequest(
                    agentPhoneNumber = localStorage.agentPhone,
                    institutionCode = localStorage.institutionCode,
                )
            )
        }
        loading = false
        if (error != null) {
            errorMessage = error.getMessage(context)
            return@produceState
        }
        errorMessage = ""
        if (response?.response == null) return@produceState
        value = response
            .response!!
            .asSequence()
            .distinctBy { it.caseReference }
            .map { caseDetail ->
                caseDetail.copy(
                    description = caseDetail.description?.trim { it <= ' ' }
                        ?.uppercase(Locale.ROOT),
                    subject = caseDetail.subject.trim { it <= ' ' }.uppercase(Locale.ROOT),
                )
            }
            .toList()
        viewModel.caseDetails.value = value
    }
    val filteredCaseDetailsGroup = remember(caseDetails) {
        caseDetails.toList()
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
    ) {
        val (appBar, list, fab) = createRefs()

        CreditClubAppBar(
            title = stringResource(R.string.support_cases),
            onBackPressed = { navController.popBackStack() },
            modifier = Modifier.constrainAs(appBar) {
                top.linkTo(parent.top)
                linkTo(
                    start = parent.start,
                    end = parent.end,
                )
            },
        )
        SwipeRefresh(
            state = rememberSwipeRefreshState(loading),
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

                items(filteredCaseDetailsGroup, key = { it.caseReference }) {
                    SupportThreadItem(caseDetail = it, onClick = {
                        navController.navigate(
                            Routes.supportConversation(
                                it.caseReference,
                                it.subject,
                            )
                        )
                    })
                }

                item {
                    Spacer(modifier = Modifier.height(96.dp))
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = {
                context.startActivity(
                    Intent(
                        context,
                        CaseLogActivity::class.java
                    )
                )
            },
            icon = { Icon(Icons.Filled.Add, "") },
            text = { Text(stringResource(id = R.string.log_case).uppercase(Locale.ROOT)) },
            backgroundColor = MaterialTheme.colors.primary,
            contentColor = MaterialTheme.colors.onPrimary,
            elevation = FloatingActionButtonDefaults.elevation(8.dp),
            modifier = Modifier
                .constrainAs(fab) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
                .padding(16.dp),
        )
    }
}

@Composable
private fun SupportThreadItem(caseDetail: CaseDetail, onClick: () -> Unit) {
    val formattedDate = remember(caseDetail.dateLogged) {
        when (val localDate = caseDetail.dateLogged.format("dd/MM/uuuu")) {
            today -> "Today"
            yesterday -> "Yesterday"
            else -> localDate
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = colorResource(R.color.ef_grey)),
            ),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                caseDetail.subject,
                color = colorResource(id = R.color.colorPrimary),
                maxLines = 1,
                style = MaterialTheme.typography.subtitle1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formattedDate,
                maxLines = 1,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 16.dp),
            )
        }
        Text(
            text = caseDetail.description ?: "",
            maxLines = 1,
            style = MaterialTheme.typography.body1,
            color = colorResource(R.color.ef_grey),
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(start = 16.dp, bottom = 10.dp, end = 16.dp)
                .fillMaxWidth(),
        )
        Divider(startIndent = 16.dp)
    }
}