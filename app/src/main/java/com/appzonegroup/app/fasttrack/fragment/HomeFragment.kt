package com.appzonegroup.app.fasttrack.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.appzonegroup.app.fasttrack.*
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.utility.logout
import com.appzonegroup.app.fasttrack.utility.openPageById
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.AppFunctions
import com.creditclub.core.data.api.NotificationService
import com.creditclub.core.data.model.AppFunctionUsage
import com.creditclub.core.data.model.NotificationRequest
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.ui.widget.DialogConfirmParams
import com.creditclub.core.util.debugOnly
import com.creditclub.core.util.delegates.service
import com.creditclub.core.util.packageInfo
import com.creditclub.core.util.safeRunIO
import com.creditclub.core.util.safeRunSuspend
import com.creditclub.ui.UpdateActivity
import com.creditclub.ui.theme.CreditClubTheme
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import dev.chrisbanes.accompanist.insets.statusBarsHeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


class HomeFragment : CreditClubFragment() {
    private val notificationViewModel by activityViewModels<NotificationViewModel>()
    private val notificationService by creditClubMiddleWareAPI.retrofit.service<NotificationService>()
    private val frequentFunctionsLive = MutableLiveData<List<AppFunctionUsage>>(emptyList())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainScope.launch { getFavorites() }
        mainScope.launch { getNotifications() }

        val hasPosUpdateManager = Platform.isPOS && Platform.deviceType != 2

        if (hasPosUpdateManager) checkForUpdate()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CreditClubTheme {
                    ProvideWindowInsets {
                        HomeContent()
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun HomeContent() {
        val coroutineScope = rememberCoroutineScope()
        val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
        val frequentFunctions = frequentFunctionsLive.observeAsState(emptyList())
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = { AppBar(scaffoldState = scaffoldState) },
            drawerContent = {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        DrawerContent(
                            scaffoldState = scaffoldState,
                            coroutineScope = coroutineScope,
                        )
                    }
                }

                if (Platform.isPOS && Platform.deviceType != 2) {
                    DrawerRow(
                        title = stringResource(R.string.update),
                        icon = painterResource(R.drawable.ic_fa_arrow_down),
                        onClick = {
                            coroutineScope.launch { scaffoldState.drawerState.close() }
                            startActivity(UpdateActivity::class.java)
                        }
                    )
                }

                debugOnly {
                    Text(
                        text = "For development use only",
                        modifier = Modifier.padding(start = 16.dp, bottom = 5.dp),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(0.5f),
                    )
                }

                Text(
                    text = "v${requireContext().packageInfo?.versionName}. Powered by CreditClub",
                    modifier = Modifier.padding(start = 16.dp),
                    style = MaterialTheme.typography.body1,
                    color = MaterialTheme.colors.onSurface.copy(0.5f),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { requireActivity().logout() },
                ) {
                    Text(
                        stringResource(R.string.logout),
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
            },
            backgroundColor = colorResource(R.color.menuBackground)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .wrapContentWidth(Alignment.Start)
                        .weight(1f),
                    horizontalArrangement = Arrangement.Start,
                ) {
                    LazyVerticalGrid(
                        cells = GridCells.Adaptive(minSize = 150.dp)
                    ) {
                        if (institutionConfig.categories.customers) {
                            item {
                                MenuButton(
                                    text = "Customer",
                                    icon = painterResource(R.drawable.payday_loan),
                                    category = AppFunctions.Categories.CUSTOMER_CATEGORY
                                )
                            }
                        }

                        item {
                            MenuButton(
                                text = "Agent",
                                icon = painterResource(R.drawable.income),
                                category = AppFunctions.Categories.AGENT_CATEGORY
                            )
                        }

                        item {
                            MenuButton(
                                text = "Transactions",
                                icon = painterResource(R.drawable.deposit),
                                category = AppFunctions.Categories.TRANSACTIONS_CATEGORY
                            )
                        }

                        if (institutionConfig.categories.loans) {
                            item {
                                MenuButton(
                                    text = "Loans",
                                    icon = painterResource(R.drawable.personal_income),
                                    category = AppFunctions.Categories.LOAN_CATEGORY
                                )
                            }
                        }
                    }
                }

                if (frequentFunctions.value.isNotEmpty()) {
                    Text(
                        text = "Frequently Used",
                        color = colorResource(R.color.menuButtonTextColor),
                        style = MaterialTheme.typography.subtitle2,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    val favModifier = Modifier
                        .weight(1f)
                        .widthIn(max = 300.dp, min = 80.dp)
                        .padding(end = 10.dp)
                        .border(
                            BorderStroke(
                                1.dp,
                                MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                            ),
                            RoundedCornerShape(15.dp),
                        )
                    val menuButtonIconTint = colorResource(R.color.menuButtonIconTint)
                    val tint = if (menuButtonIconTint.alpha == 0f) {
                        LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
                    } else {
                        menuButtonIconTint
                    }
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                            .wrapContentWidth()
                    ) {
                        frequentFunctions.value.forEach { appFunctionUsage ->
                            AppFunctions[appFunctionUsage.fid]?.run {
                                TextButton(
                                    onClick = { openPageById(id) },
                                    shape = RoundedCornerShape(15.dp),
                                    modifier = favModifier
                                ) {
                                    if (icon != null) {
                                        Icon(
                                            painter = painterResource(icon!!),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = tint,
                                        )
                                    }
                                    Text(
                                        text = stringResource(label).toUpperCase(),
                                        color = colorResource(R.color.menuButtonTextColor),
                                        style = MaterialTheme.typography.caption,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(start = 5.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DrawerContent(
        scaffoldState: ScaffoldState,
        coroutineScope: CoroutineScope
    ) {
        val agent = localStorage.agent
        val logoTint = colorResource(R.color.navHeaderLogoTint)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.primary)
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            Image(
                painter = painterResource(R.drawable.ic_launcher_transparent),
                colorFilter = if (logoTint == Color.Transparent) null else ColorFilter.tint(logoTint),
                contentDescription = null,
                modifier = Modifier
                    .height(50.dp)
                    .width(IntrinsicSize.Max)
                    .padding(16.dp)
                    .widthIn(),
            )
            Text(
                text = agent?.agentName ?: "",
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp),
                softWrap = true,
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onPrimary,
            )
            Text(
                text = agent?.phoneNumber ?: "",
                modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onPrimary.copy(0.5f),
            )
        }

        if (institutionConfig.hasOnlineFunctions) {
            DrawerRow(
                title = stringResource(R.string.online_functions),
                icon = painterResource(R.drawable.ic_fa_arrow_up),
                onClick = {
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                    startActivity(Intent(requireContext(), OnlineActivity::class.java))
                }
            )
        }

        DrawerRow(
            title = stringResource(R.string.reports),
            icon = painterResource(R.drawable.ic_agent_balance),
            onClick = {
                coroutineScope.launch { scaffoldState.drawerState.close() }
                startActivity(Intent(requireContext(), ReportActivity::class.java))
            }
        )

        DrawerRow(
            title = stringResource(R.string.commission),
            icon = painterResource(R.drawable.ic_agent_balance),
            onClick = {
                coroutineScope.launch { scaffoldState.drawerState.close() }
                startActivity(Intent(requireContext(), CommissionsActivity::class.java))
            }
        )

        DrawerRow(
            title = stringResource(R.string.support),
            icon = painterResource(R.drawable.ic_chat_bubble_outline),
            onClick = {
                coroutineScope.launch { scaffoldState.drawerState.close() }
                startActivity(Intent(requireContext(), SupportActivity::class.java))
            }
        )

        if (institutionConfig.hasHlaTagging) {
            DrawerRow(
                title = stringResource(R.string.hla_tagging),
                icon = painterResource(R.drawable.ic_maps_and_flags),
                onClick = {
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                    startActivity(HlaTaggingActivity::class.java)
                }
            )
        }

        DrawerRow(
            title = stringResource(R.string.title_activity_faq),
            icon = painterResource(R.drawable.ic_help),
            onClick = {
                coroutineScope.launch { scaffoldState.drawerState.close() }
                startActivity(FaqActivity::class.java)
            }
        )
    }

    @Composable
    private fun AppBar(scaffoldState: ScaffoldState) {
        val appBarColor = MaterialTheme.colors.surface.copy(alpha = 0.87f)
        val coroutineScope = rememberCoroutineScope()
        // Draw a scrim over the status bar which matches the app bar
        Spacer(
            Modifier
                .background(appBarColor)
                .fillMaxWidth()
                .statusBarsHeight()
        )
        TopAppBar(
            title = {
                val toolbarLogoTint = colorResource(R.color.toolbarLogoTint)
                Row {
                    Image(
                        painterResource(R.drawable.ic_logo_with_name),
                        contentDescription = null,
                        colorFilter = if (toolbarLogoTint == Color.Transparent) null else ColorFilter.tint(
                            toolbarLogoTint
                        ),
                        modifier = Modifier
                            .heightIn(max = 50.dp)
                            .widthIn(max = 140.dp)
                            .weight(1f),
                    )
                    IconButton(
                        onClick = {
                            findNavController().navigate(HomeFragmentDirections.homeToNotifications())
                        }
                    ) {
                        Icon(
                            Icons.Filled.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colors.onPrimary.copy(0.52f)
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    coroutineScope.launch {
                        safeRunSuspend {
                            scaffoldState.drawerState.open()
                        }
                    }
                }) {
                    Icon(
                        Icons.Default.Menu,
                        contentDescription = null,
                        tint = MaterialTheme.colors.onPrimary.copy(0.52f)
                    )
                }
            },
            elevation = 5.dp
        )
    }

    private suspend fun getNotifications() {
        val (response) = safeRunIO {
            notificationService.getNotifications(
                NotificationRequest(
                    localStorage.agentPhone,
                    localStorage.institutionCode,
                    20,
                    0
                )
            )
        }

        if (response != null) notificationViewModel.notificationList.value =
            response.response ?: emptyList()
    }

    @Composable
    private fun MenuButton(
        text: String,
        icon: Painter,
        category: Int,
    ) {
        val menuButtonIconTint = colorResource(R.color.menuButtonIconTint)
        Card(
            modifier = Modifier
                .heightIn(150.dp, 200.dp)
                .widthIn(150.dp, 300.dp)
                .padding(10.dp),
            elevation = 2.dp,
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.clickable(
                    onClick = {
                        findNavController().navigate(
                            HomeFragmentDirections.actionHomeToSubMenu(category)
                        )
                    }
                ),
            ) {
                Image(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp),
                    colorFilter = if (menuButtonIconTint.alpha == 0f) null else ColorFilter.tint(
                        menuButtonIconTint
                    )
                )
                Text(
                    text = text.toUpperCase(),
                    style = MaterialTheme.typography.button,
                    color = colorResource(R.color.menuButtonTextColor),
                    modifier = Modifier.padding(10.dp),
                )
            }
        }
    }

    private fun checkForUpdate() = appDataStorage.latestVersion?.run {
        val currentVersion = requireContext().packageInfo?.versionName
        if (currentVersion != null && updateIsAvailable(currentVersion)) {
            val updateIsRequired = updateIsRequired(currentVersion)
            val mustUpdate = updateIsRequired && daysOfGraceLeft() < 1
            val message = "A new version (v$version) is available."
            val subtitle =
                if (updateIsRequired && mustUpdate) "You need to update now"
                else if (updateIsRequired) "Please update with ${daysOfGraceLeft()} days"
                else "Please update"

            dialogProvider.confirm(DialogConfirmParams(message, subtitle)) {
                onSubmit {
                    if (it) {
                        startActivity(
                            Intent(
                                requireContext(),
                                UpdateActivity::class.java
                            )
                        )
                        requireActivity().finish()
                    } else if (mustUpdate) requireActivity().finish()
                }

                onClose {
                    if (mustUpdate) requireActivity().finish()
                }
            }
        }
    }

    private suspend fun getFavorites() {
        val (list) = safeRunIO {
            coreDatabase.appFunctionUsageDao().getMostUsed()
        }

        frequentFunctionsLive.value = list
    }
}

@Composable
private fun DrawerRow(
    title: String,
    icon: Painter,
    onClick: () -> Unit
) {
    val background = Color.Transparent
    val textColor = MaterialTheme.colors.onSurface.copy(0.5f)
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(background)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            icon,
            contentDescription = null,
            colorFilter = ColorFilter.tint(textColor),
            modifier = Modifier
                .padding(16.dp)
                .size(24.dp),
        )
        Text(color = textColor, text = title)
    }
}
