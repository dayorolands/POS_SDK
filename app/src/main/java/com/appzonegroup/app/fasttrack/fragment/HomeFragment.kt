package com.appzonegroup.app.fasttrack.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyGridScope
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.navigation.fragment.findNavController
import com.appzonegroup.app.fasttrack.*
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.ui.FundsTransfer
import com.appzonegroup.app.fasttrack.utility.logout
import com.appzonegroup.app.fasttrack.utility.openPageById
import com.appzonegroup.creditclub.pos.Platform
import com.creditclub.Routes
import com.creditclub.components.*
import com.creditclub.conversation.ConversationContent
import com.creditclub.conversation.LocalBackPressedDispatcher
import com.creditclub.core.config.IInstitutionConfig
import com.creditclub.core.data.api.NotificationService
import com.creditclub.core.data.api.retrofitService
import com.creditclub.core.data.model.NotificationRequest
import com.creditclub.core.ui.CreditClubFragment
import com.creditclub.core.ui.widget.DialogConfirmParams
import com.creditclub.core.util.*
import com.creditclub.screen.PinChange
import com.creditclub.screen.SupportCases
import com.creditclub.screen.UssdWithdrawal
import com.creditclub.ui.UpdateActivity
import com.creditclub.ui.rememberBean
import com.creditclub.ui.theme.CreditClubTheme
import com.creditclub.viewmodel.AppViewModel
import com.google.accompanist.insets.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import java.util.*

private val composableRouteFunctionIds = mapOf(
    R.id.agent_change_pin_button to Routes.PinChange,
    R.id.funds_transfer_button to Routes.FundsTransfer,
    R.id.fn_support to Routes.SupportCases,
    R.id.ussd_withdrawal_button to Routes.UssdWithdrawal,
)

class HomeFragment : CreditClubFragment() {
    private val notificationViewModel: NotificationViewModel by activityViewModels()
    private val appViewModel: AppViewModel by viewModels()
    private val notificationService: NotificationService by retrofitService()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainScope.launch { getNotifications() }

        val hasPosUpdateManager = Platform.isPOS && Platform.deviceType != 2

        if (hasPosUpdateManager) checkForUpdate()

        loadFcmToken()
    }

    @OptIn(ExperimentalAnimatedInsets::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentNavController = findNavController()
        return ComposeView(inflater.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )

            // Create a ViewWindowInsetObserver using this view, and call start() to
            // start listening now. The WindowInsets instance is returned, allowing us to
            // provide it to AmbientWindowInsets in our content below.
            val windowInsets = ViewWindowInsetObserver(this)
                // We use the `windowInsetsAnimationsEnabled` parameter to enable animated
                // insets support. This allows our `ConversationContent` to animate with the
                // on-screen keyboard (IME) as it enters/exits the screen.
                .start(windowInsetsAnimationsEnabled = true)

            setContent {
                val composeNavController = rememberNavController()
                CompositionLocalProvider(
                    LocalBackPressedDispatcher provides requireActivity().onBackPressedDispatcher,
                    LocalWindowInsets provides windowInsets,
                ) {
                    CreditClubTheme {
                        ProvideWindowInsets {
                            NavHost(navController = composeNavController, Routes.Home) {
                                composable(Routes.Home) {
                                    HomeContent(
                                        mainNavController = fragmentNavController,
                                        composeNavController = composeNavController,
                                    )
                                }
                                composable(Routes.FundsTransfer) {
                                    FundsTransfer(
                                        navController = composeNavController,
                                        dialogProvider = dialogProvider,
                                    )
                                }
                                composable(Routes.PinChange) {
                                    PinChange(navController = composeNavController)
                                }
                                composable(Routes.SupportCases) {
                                    SupportCases(navController = composeNavController)
                                }
                                composable(Routes.SupportConversation) { backStackEntry ->
                                    val fcmToken by appViewModel.fcmToken
                                    val arguments = backStackEntry.arguments!!
                                    val reference = arguments.getString("reference")!!
                                    val title = arguments.getString("title")

                                    ConversationContent(
                                        title = title ?: "Case",
                                        fcmToken = fcmToken,
                                        reference = reference,
                                        onNavIconPressed = { composeNavController.popBackStack() },
                                        navController = composeNavController,
                                        // Add padding so that we are inset from any left/right navigation bars
                                        // (usually shown when in landscape orientation)
                                        modifier = Modifier.navigationBarsPadding(bottom = false),
                                        navigateToProfile = {},
                                    )
                                }
                                composable(Routes.UssdWithdrawal) {
                                    UssdWithdrawal(navController = composeNavController)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalStdlibApi::class)
    @Composable
    private fun HomeContent(
        mainNavController: NavController,
        composeNavController: NavHostController,
    ) {
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
                HomeAppBar(
                    scaffoldState = scaffoldState,
                    mainNavController = mainNavController,
                )
            },
            bottomBar = { SubMenuBottomNavigation(homeNavController, bottomNavigationItems) },
            drawerContent = {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        DrawerContent(
                            scaffoldState = scaffoldState,
                            coroutineScope = coroutineScope,
                            openPage = {
                                if (composableRouteFunctionIds.containsKey(it)) {
                                    composeNavController.navigate(composableRouteFunctionIds[it]!!)
                                } else {
                                    openPageById(it)
                                }
                            },
                        )
                    }
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
                Column(
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .weight(1f),
                ) {
                    Text(
                        title,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.h4,
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    )
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
                                        onClick = {
                                            composeNavController.navigate(Routes.PinChange)
                                        }
                                    )
                                }
                                if (flows.customerBalance != null) {
                                    item {
                                        MenuButton(
                                            text = "Balance Enquiry",
                                            icon = painterResource(R.drawable.income),
                                            onClick = { openPageById(R.id.agent_balance_enquiry_button) }
                                        )
                                    }
                                }
                            }
                        }
                        composable(BottomNavScreens.Customer.route) {
                            SubMenu {
                                if (flows.accountOpening != null) {
                                    item {
                                        MenuButton(
                                            text = "Register",
                                            icon = painterResource(R.drawable.payday_loan),
                                            onClick = { openPageById(R.id.register_button) }
                                        )
                                    }
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
                            LazyVerticalGrid(
                                cells = GridCells.Adaptive(minSize = 100.dp)
                            ) {
                                item {
                                    SmallMenuButton(
                                        text = "Deposit",
                                        icon = painterResource(R.drawable.deposit),
                                        onClick = { openPageById(R.id.deposit_button) }
                                    )
                                }
                                if (flows.tokenWithdrawal != null) {
                                    item {
                                        SmallMenuButton(
                                            text = "Token Withdrawal",
                                            icon = painterResource(R.drawable.withdraw),
                                            onClick = { openPageById(R.id.token_withdrawal_button) }
                                        )
                                    }
                                }
                                if (flows.tokenWithdrawal != null) {
                                    item {
                                        SmallMenuButton(
                                            text = "USSD Withdrawal",
                                            icon = painterResource(R.drawable.withdraw),
                                            onClick = {
                                                composeNavController.navigate(Routes.UssdWithdrawal)
                                            }
                                        )
                                    }
                                }
                                if (Platform.isPOS) {
                                    item {
                                        SmallMenuButton(
                                            text = "Card Transactions",
                                            icon = painterResource(R.drawable.withdraw),
                                            onClick = { openPageById(R.id.card_withdrawal_button) }
                                        )
                                    }
                                }
                                if (flows.billPayment != null) {
                                    item {
                                        SmallMenuButton(
                                            text = "Bills Payment",
                                            icon = painterResource(R.drawable.payday_loan),
                                            onClick = { openPageById(R.id.pay_bill_button) }
                                        )
                                    }
                                }
                                if (flows.airtime != null) {
                                    item {
                                        SmallMenuButton(
                                            text = "Airtime Topup",
                                            icon = painterResource(R.drawable.payday_loan),
                                            onClick = { openPageById(R.id.airtime_button) }
                                        )
                                    }
                                }
                                item {
                                    SmallMenuButton(
                                        text = "Funds Transfer",
                                        icon = painterResource(R.drawable.payday_loan),
                                        onClick = { openPageById(R.id.funds_transfer_button) }
                                    )
                                }
                                if (flows.collectionPayment != null) {
                                    item {
                                        SmallMenuButton(
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
                    FrequentlyUsed(onItemClick = {
                        if (composableRouteFunctionIds.containsKey(it)) {
                            composeNavController.navigate(composableRouteFunctionIds[it]!!)
                        } else {
                            openPageById(it)
                        }
                    })
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

        if (response?.response != null) {
            notificationViewModel.notificationList.value = response.response!!
            appViewModel.notificationList.value = response.response!!
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

    private fun loadFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("Support", "Fetching FCM registration token failed", task.exception)
                loadFcmToken()
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            appViewModel.fcmToken.value = token
        }
    }
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


