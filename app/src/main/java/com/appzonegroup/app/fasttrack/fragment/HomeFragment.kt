package com.appzonegroup.app.fasttrack.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyGridScope
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.fragment.findNavController
import com.appzonegroup.app.fasttrack.*
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.utility.logout
import com.appzonegroup.app.fasttrack.utility.openPageById
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.core.AppFunctions
import com.creditclub.core.config.IInstitutionConfig
import com.creditclub.core.data.CoreDatabase
import com.creditclub.core.data.api.NotificationService
import com.creditclub.core.data.api.StaticService
import com.creditclub.core.data.model.AppFunctionUsage
import com.creditclub.core.data.model.NotificationRequest
import com.creditclub.core.data.prefs.LocalStorage
import com.creditclub.core.data.request.BalanceEnquiryRequest
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.ui.widget.DialogConfirmParams
import com.creditclub.core.util.*
import com.creditclub.core.util.delegates.service
import com.creditclub.ui.UpdateActivity
import com.creditclub.ui.rememberBean
import com.creditclub.ui.rememberDialogProvider
import com.creditclub.ui.rememberRetrofitService
import com.creditclub.ui.theme.CreditClubTheme
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import dev.chrisbanes.accompanist.insets.statusBarsHeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*


class HomeFragment : CreditClubFragment() {
    private val notificationViewModel by activityViewModels<NotificationViewModel>()
    private val notificationService by creditClubMiddleWareAPI.retrofit.service<NotificationService>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainScope.launch { getNotifications() }

        val hasPosUpdateManager = Platform.isPOS && Platform.deviceType != 2

        if (hasPosUpdateManager) checkForUpdate()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mainNavController = findNavController()
        return ComposeView(requireContext()).apply {
            setContent {
                CreditClubTheme {
                    ProvideWindowInsets {
                        HomeContent(mainNavController = mainNavController)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalStdlibApi::class)
    @Composable
    private fun HomeContent(mainNavController: NavController) {
        val institutionConfig: IInstitutionConfig by rememberBean()
        val flows = institutionConfig.flows
        val homeNavController = rememberNavController()
        val coroutineScope = rememberCoroutineScope()
        val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
        val bottomNavigationItems = remember {
            buildList {
                add(BottomNavScreens.Home)
                if (institutionConfig.categories.customers) {
                    add(BottomNavScreens.Customer)
                }
                add(BottomNavScreens.Agent)
                add(BottomNavScreens.Transactions)
                if (institutionConfig.categories.loans) {
                    add(BottomNavScreens.Loans)
                }
            }
        }
        val currentRoute = currentRoute(homeNavController)
        val context = LocalContext.current
        val greetingMessage = remember {
            when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                in 0..11 -> "Good Morning"
                in 12..15 -> "Good Afternoon"
                in 16..23 -> "Good Evening"
                else -> "Good Day"
            }
        }
        val title = when (currentRoute) {
            BottomNavScreens.Agent.route -> "Agent"
            BottomNavScreens.Transactions.route -> "Transactions"
            BottomNavScreens.Customer.route -> "Customer"
            else -> greetingMessage
        }

        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                AppBar(
                    scaffoldState = scaffoldState,
                    mainNavController = mainNavController
                )
            },
            bottomBar = { SubMenuBottomNavigation(homeNavController, bottomNavigationItems) },
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
                    text = "v${context.packageInfo?.versionName}. Powered by CreditClub",
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
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Text(
                    title,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .weight(1f),
                ) {
                    NavHost(
                        homeNavController,
                        startDestination = BottomNavScreens.Home.route
                    ) {
                        composable(BottomNavScreens.Home.route) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                BalanceCard()
                                LazyVerticalGrid(
                                    cells = GridCells.Adaptive(minSize = 100.dp)
                                ) {
                                    if (institutionConfig.categories.customers) {
                                        item {
                                            SmallMenuButton(
                                                text = "Customer",
                                                icon = painterResource(R.drawable.payday_loan),
                                                onClick = {
                                                    homeNavController.navigate(
                                                        BottomNavScreens.Customer.route
                                                    ) {
                                                        launchSingleTop = true
                                                        popUpTo(BottomNavScreens.Home.route) {
                                                            inclusive = false
                                                        }
                                                    }
                                                },
                                            )
                                        }
                                    }

                                    item {
                                        SmallMenuButton(
                                            text = "Agent",
                                            icon = painterResource(R.drawable.income),
                                            onClick = {
                                                homeNavController.navigate(BottomNavScreens.Agent.route) {
                                                    launchSingleTop = true
                                                    popUpTo(BottomNavScreens.Home.route) {
                                                        inclusive = false
                                                    }
                                                }
                                            },
                                        )
                                    }

                                    item {
                                        SmallMenuButton(
                                            text = "Transactions",
                                            icon = painterResource(R.drawable.deposit),
                                            onClick = {
                                                homeNavController.navigate(BottomNavScreens.Transactions.route) {
                                                    launchSingleTop = true
                                                    popUpTo(BottomNavScreens.Home.route) {
                                                        inclusive = false
                                                    }
                                                }
                                            },
                                        )
                                    }

                                    if (institutionConfig.categories.loans) {
                                        item {
                                            SmallMenuButton(
                                                text = "Loans",
                                                icon = painterResource(R.drawable.personal_income),
                                                onClick = {
                                                    homeNavController.navigate(
                                                        BottomNavScreens.Loans.route
                                                    ) {
                                                        launchSingleTop = true
                                                        popUpTo(BottomNavScreens.Home.route) {
                                                            inclusive = false
                                                        }
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        composable(BottomNavScreens.Agent.route) {
                            SubMenu {
                                item {
                                    MenuButton(
                                        text = "Mini Statement",
                                        icon = painterResource(R.drawable.deposit),
                                        onClick = { openPageById(R.id.agent_mini_statement_button) }
                                    )
                                }
                                item {
                                    MenuButton(
                                        text = "Change PIN",
                                        icon = painterResource(R.drawable.login_password),
                                        onClick = { openPageById(R.id.agent_change_pin_button) }
                                    )
                                }
                                item {
                                    MenuButton(
                                        text = "Balance Enquiry",
                                        icon = painterResource(R.drawable.income),
                                        onClick = { openPageById(R.id.agent_balance_enquiry_button) }
                                    )
                                }
                            }
                        }
                        composable(BottomNavScreens.Customer.route) {
                            SubMenu {
                                item {
                                    MenuButton(
                                        text = "Register",
                                        icon = painterResource(R.drawable.payday_loan),
                                        onClick = { openPageById(R.id.register_button) }
                                    )
                                }
                                if (flows.walletOpening != null) {
                                    item {
                                        MenuButton(
                                            text = "New Wallet",
                                            icon = painterResource(R.drawable.payday_loan),
                                            onClick = { openPageById(R.id.new_wallet_button) }
                                        )
                                    }
                                }
                                item {
                                    MenuButton(
                                        text = "Balance Enquiry",
                                        icon = painterResource(R.drawable.income),
                                        onClick = { openPageById(R.id.customer_balance_enquiry_button) }
                                    )
                                }
                                if (flows.customerPinChange != null) {
                                    item {
                                        MenuButton(
                                            text = "Change PIN",
                                            icon = painterResource(R.drawable.login_password),
                                            onClick = { openPageById(R.id.customer_change_pin_button) }
                                        )
                                    }
                                }
                                if (flows.bvnUpdate != null) {
                                    item {
                                        MenuButton(
                                            text = "BVN Update",
                                            icon = painterResource(R.drawable.secured_loan),
                                            onClick = { openPageById(R.id.bvn_update_button) }
                                        )
                                    }
                                }
                            }
                        }
                        composable(BottomNavScreens.Transactions.route) {
                            SubMenu {
                                item {
                                    MenuButton(
                                        text = "Deposit",
                                        icon = painterResource(R.drawable.deposit),
                                        onClick = { openPageById(R.id.deposit_button) }
                                    )
                                }
                                item {
                                    MenuButton(
                                        text = "Token Withdrawal",
                                        icon = painterResource(R.drawable.withdraw),
                                        onClick = { openPageById(R.id.token_withdrawal_button) }
                                    )
                                }
                                if (Platform.isPOS) {
                                    item {
                                        MenuButton(
                                            text = "Card Transactions",
                                            icon = painterResource(R.drawable.withdraw),
                                            onClick = { openPageById(R.id.card_withdrawal_button) }
                                        )
                                    }
                                }
                                if (flows.billPayment != null) {
                                    item {
                                        MenuButton(
                                            text = "Bills Payment",
                                            icon = painterResource(R.drawable.payday_loan),
                                            onClick = { openPageById(R.id.pay_bill_button) }
                                        )
                                    }
                                }
                                item {
                                    MenuButton(
                                        text = "Airtime Topup",
                                        icon = painterResource(R.drawable.payday_loan),
                                        onClick = { openPageById(R.id.airtime_button) }
                                    )
                                }
                                item {
                                    MenuButton(
                                        text = "Funds Transfer",
                                        icon = painterResource(R.drawable.payday_loan),
                                        onClick = { openPageById(R.id.funds_transfer_button) }
                                    )
                                }
                                if (flows.collectionPayment != null) {
                                    item {
                                        MenuButton(
                                            text = "IGR Collections",
                                            icon = painterResource(R.drawable.payday_loan),
                                            onClick = { openPageById(R.id.collection_payment_button) }
                                        )
                                    }
                                }
                            }
                        }
                        composable(BottomNavScreens.Loans.route) {
                            SubMenu {
                                item {
                                    MenuButton(
                                        text = "Loan Request",
                                        icon = painterResource(R.drawable.personal_income),
                                        onClick = { openPageById(R.id.loan_request_button) }
                                    )
                                }
                            }
                        }
                    }
                }

                if (currentRoute == BottomNavScreens.Home.route) {
                    FrequentlyUsed(onItemClick = ::openPageById)
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )
            }
        }
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
                                context,
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
}

@Composable
private fun DrawerContent(
    scaffoldState: ScaffoldState,
    coroutineScope: CoroutineScope
) {
    val localStorage: LocalStorage by rememberBean()
    val institutionConfig: IInstitutionConfig by rememberBean()
    val agent = localStorage.agent
    val context = LocalContext.current
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
                context.startActivity(Intent(context, OnlineActivity::class.java))
            }
        )
    }

    DrawerRow(
        title = stringResource(R.string.reports),
        icon = painterResource(R.drawable.ic_agent_balance),
        onClick = {
            coroutineScope.launch { scaffoldState.drawerState.close() }
            context.startActivity(Intent(context, ReportActivity::class.java))
        }
    )

    DrawerRow(
        title = stringResource(R.string.commission),
        icon = painterResource(R.drawable.ic_agent_balance),
        onClick = {
            coroutineScope.launch { scaffoldState.drawerState.close() }
            context.startActivity(Intent(context, CommissionsActivity::class.java))
        }
    )

    DrawerRow(
        title = stringResource(R.string.support),
        icon = painterResource(R.drawable.ic_chat_bubble_outline),
        onClick = {
            coroutineScope.launch { scaffoldState.drawerState.close() }
            context.startActivity(Intent(context, SupportActivity::class.java))
        }
    )

    if (institutionConfig.hasHlaTagging) {
        DrawerRow(
            title = stringResource(R.string.hla_tagging),
            icon = painterResource(R.drawable.ic_maps_and_flags),
            onClick = {
                coroutineScope.launch { scaffoldState.drawerState.close() }
                context.startActivity(Intent(context, HlaTaggingActivity::class.java))
            }
        )
    }

    DrawerRow(
        title = stringResource(R.string.title_activity_faq),
        icon = painterResource(R.drawable.ic_help),
        onClick = {
            coroutineScope.launch { scaffoldState.drawerState.close() }
            context.startActivity(Intent(context, FaqActivity::class.java))
        }
    )
}

@Composable
private fun AppBar(scaffoldState: ScaffoldState, mainNavController: NavController) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(
                    modifier = Modifier
                        .weight(1f),
                )
                IconButton(
                    onClick = {
                        mainNavController.navigate(HomeFragmentDirections.homeToNotifications())
                    }
                ) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary.copy(0.52f)
                    )
                }
            }
        },
        backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0f),
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
                    tint = MaterialTheme.colors.primary.copy(0.52f)
                )
            }
        },
        elevation = 0.dp
    )
}


@Composable
private fun SubMenuBottomNavigation(
    navController: NavHostController,
    bottomNavigationItems: List<BottomNavScreens>
) {
    val currentRoute = currentRoute(navController)
    Divider()
    BottomNavigation(
        backgroundColor = colorResource(R.color.menuBackground),
        contentColor = MaterialTheme.colors.onSurface,
        elevation = 0.dp,
    ) {
        for (screen in bottomNavigationItems) {
            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(screen.icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = { Text(stringResource(id = screen.resourceId)) },
                selected = currentRoute == screen.route,
                onClick = {
                    // This if check gives us a "singleTop" behavior where we do not create a
                    // second instance of the composable if we are already on that destination
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            launchSingleTop = true
                            popUpTo(BottomNavScreens.Home.route) {
                                inclusive = screen.route == BottomNavScreens.Home.route
                            }
                        }
                    }
                }
            )
        }
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

@Composable
private fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.arguments?.getString(KEY_ROUTE)
}

private sealed class BottomNavScreens(
    val route: String,
    @StringRes val resourceId: Int,
    @DrawableRes val icon: Int
) {
    object Home : BottomNavScreens("Home", R.string.home, R.drawable.ic_home)
    object Agent : BottomNavScreens("Agent", R.string.agent, R.drawable.income)
    object Customer : BottomNavScreens("Customer", R.string.customer, R.drawable.payday_loan)
    object Transactions :
        BottomNavScreens("Transactions", R.string.transactions, R.drawable.deposit)

    object Loans : BottomNavScreens("Loans", R.string.loans, R.drawable.personal_income)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubMenu(content: LazyGridScope.() -> Unit) {
    LazyVerticalGrid(
        cells = GridCells.Adaptive(minSize = 150.dp)
    ) {
        content()
    }
}

@Composable
private fun MenuButton(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
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
            modifier = Modifier.clickable(onClick = onClick),
        ) {
            Image(
                icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .size(35.dp),
                colorFilter = if (menuButtonIconTint.alpha == 0f) null else ColorFilter.tint(
                    menuButtonIconTint
                )
            )
            Text(
                text = text.toUpperCase(),
                style = MaterialTheme.typography.button,
                color = colorResource(R.color.menuButtonTextColor),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp),
            )
        }
    }
}

@Composable
private fun SmallMenuButton(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
) {
    val menuButtonIconTint = colorResource(R.color.menuButtonIconTint)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .heightIn(100.dp, 150.dp)
            .widthIn(120.dp, 200.dp)
            .padding(8.dp),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .weight(1f)
                .clickable(onClick = onClick),
            elevation = 2.dp,
            shape = RoundedCornerShape(20.dp),
        ) {
            Image(
                icon,
                contentDescription = null,
                alignment = Alignment.Center,
                modifier = Modifier
                    .padding(30.dp)
                    .size(35.dp),
                colorFilter = if (menuButtonIconTint.alpha == 0f) null else ColorFilter.tint(
                    menuButtonIconTint
                )
            )
        }

        Text(
            text = text.toUpperCase(),
            style = MaterialTheme.typography.button,
            color = colorResource(R.color.menuButtonTextColor),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FrequentlyUsed(onItemClick: (id: Int) -> Unit) {
    val coreDatabase: CoreDatabase by rememberBean()
    val frequentFunctions = produceState(emptyList<AppFunctionUsage>()) {
        val (list) = safeRunIO {
            coreDatabase.appFunctionUsageDao().getMostUsed()
        }
        value = list ?: emptyList()
    }
    if (frequentFunctions.value.isNotEmpty()) {
        Column {
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
                            onClick = { onItemClick(id) },
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

@Composable
private fun BalanceCard() {
    var balance by remember { mutableStateOf(0.0) }
    var availableBalance by remember { mutableStateOf(0.0) }
    var loading by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    val balanceFormatted = remember(availableBalance, visible) {
        if (visible) availableBalance.toCurrencyFormat()
        else "XXX,XXX.XX"
    }
    val ledgerBalanceFormatted = remember(balance, visible) {
        if (visible) balance.toCurrencyFormat()
        else "XX,XXX.XX"
    }
    val dialogProvider by rememberDialogProvider()
    val localStorage: LocalStorage by rememberBean()
    val staticService: StaticService by rememberRetrofitService()
    val networkErrorMessage = stringResource(R.string.a_network_error_occurred)
    val loadBalance: suspend CoroutineScope.() -> Unit = remember {
        loadBalance@{
            val pin = dialogProvider.getPin("Agent PIN") ?: return@loadBalance
            if (pin.length != 4) {
                dialogProvider.showError("Agent PIN must be 4 digits")
                return@loadBalance
            }
            val request = BalanceEnquiryRequest().apply {
                agentPin = pin
                agentPhoneNumber = localStorage.agentPhone
                institutionCode = localStorage.institutionCode
            }

            loading = true
            val (response) = safeRunIO { staticService.balanceEnquiry(request) }
            loading = false

            if (response == null) return@loadBalance
            if (!response.isSussessful) {
                dialogProvider.showError(response.responseMessage ?: networkErrorMessage)
                return@loadBalance
            }

            availableBalance = response.availableBalance
            balance = response.balance
            visible = true
        }
    }
    val coroutineScope = rememberCoroutineScope()
    Card(
        modifier = Modifier
            .heightIn(100.dp, 170.dp)
            .fillMaxWidth()
            .padding(6.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.secondary,
        contentColor = MaterialTheme.colors.onSecondary,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(16.dp),
        ) {
            Text(
                text = balanceFormatted,
                style = MaterialTheme.typography.h3,
                color = MaterialTheme.colors.onSecondary,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .weight(1f),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "LEDGER BALANCE: $ledgerBalanceFormatted",
                    style = MaterialTheme.typography.button,
                    color = MaterialTheme.colors.onSecondary,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .weight(1f),
                )
                if (loading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colors.onSecondary,
                        modifier = Modifier
                            .size(20.dp)
                    )
                } else {
                    IconButton(
                        onClick = {
                            if (visible) visible = false
                            else coroutineScope.launch(block = loadBalance)
                        },
                    ) {
                        Icon(
                            imageVector = if (visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}