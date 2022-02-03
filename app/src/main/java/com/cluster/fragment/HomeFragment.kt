package com.cluster.fragment

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyGridScope
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import com.cluster.R
import com.cluster.clusterNavigation
import com.cluster.utility.logout
import com.cluster.utility.openPageById
import com.cluster.pos.Platform
import com.cluster.Routes
import com.cluster.activity.UpdateActivity
import com.cluster.components.*
import com.cluster.conversation.LocalBackPressedDispatcher
import com.cluster.core.config.InstitutionConfig
import com.cluster.core.data.api.NotificationService
import com.cluster.core.data.api.retrofitService
import com.cluster.core.data.model.NotificationRequest
import com.cluster.core.data.prefs.AppDataStorage
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.ui.widget.DialogConfirmParams
import com.cluster.core.util.debugOnly
import com.cluster.core.util.packageInfo
import com.cluster.core.util.safeRunIO
import com.cluster.ui.rememberBean
import com.cluster.ui.theme.CreditClubTheme
import com.cluster.viewmodel.AppViewModel
import com.google.accompanist.insets.ExperimentalAnimatedInsets
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.ViewWindowInsetObserver
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import java.util.*

private val composableRouteFunctionIds = mapOf(
    R.id.agent_change_pin_button to Routes.PinChange,
    R.id.funds_transfer_button to Routes.FundsTransfer,
    R.id.fn_support to Routes.SupportCases,
    R.id.ussd_withdrawal_button to Routes.UssdWithdrawal,
    R.id.fn_pending_transactions to Routes.PendingTransactions,
)

class HomeFragment : CreditClubFragment() {
    private val notificationViewModel: NotificationViewModel by activityViewModels()
    private val appViewModel: AppViewModel by viewModels()
    private val notificationService: NotificationService by retrofitService()
    private val appDataStorage: AppDataStorage by inject()

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
        savedInstanceState: Bundle?,
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
                            NavHost(
                                navController = composeNavController,
                                startDestination = Routes.Home,
                            ) {
                                composable(Routes.Home) {
                                    HomeContent(
                                        mainNavController = fragmentNavController,
                                        composeNavController = composeNavController,
                                    )
                                }
                                clusterNavigation(
                                    navController = composeNavController,
                                    dialogProvider = dialogProvider,
                                    appViewModel = appViewModel,
                                )
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
        val institutionConfig: InstitutionConfig by rememberBean()
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
                        text = "For testing purposes only",
                        modifier = Modifier.padding(start = 16.dp, bottom = 5.dp),
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(0.5f),
                    )
                }

                Text(
                    text = "v${context.packageInfo?.versionName}. Powered by Cluster",
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
                                            text = stringResource(R.string.account_opening),
                                            icon = painterResource(R.drawable.payday_loan),
                                            onClick = { openPageById(R.id.register_button) }
                                        )
                                    }
                                }
                                if (flows.walletOpening != null) {
                                    item {
                                        MenuButton(
                                            text = stringResource(R.string.title_activity_new_wallet),
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
                                if (flows.ussdWithdrawal != null) {
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

    private fun checkForUpdate() {
        val latestVersion = appDataStorage.latestVersion ?: return
        val currentVersion = requireContext().packageInfo!!.versionName
        if (!latestVersion.isNewerThan(currentVersion)) {
            return
        }
        val canUpdate = latestVersion.updateIsRequired(currentVersion)
        val mustUpdate = canUpdate && latestVersion.daysOfGraceLeft() < 1
        val message = "A new version (v${latestVersion.version}) is available."
        val subtitle = when {
            canUpdate && mustUpdate -> "You need to update now"
            canUpdate -> "Kindly update within ${latestVersion.daysOfGraceLeft()} days"
            else -> "Kindly update"
        }

        val latestApkFile = appDataStorage.latestApkFile
        if (latestApkFile?.exists() == true) {
            dialogProvider.confirm(DialogConfirmParams(message, subtitle)) {
                onSubmit {
                    if (it) {
                        openApk(latestApkFile)
                        if (mustUpdate) {
                            requireActivity().finish()
                        }
                    } else if (mustUpdate) requireActivity().finish()
                }

                onClose {
                    if (mustUpdate) requireActivity().finish()
                }
            }
            return
        }

        dialogProvider.confirm(DialogConfirmParams(message, subtitle)) {
            onSubmit {
                if (it) {
                    val intent = Intent(requireContext(), UpdateActivity::class.java)
                    startActivity(intent)
                    if (mustUpdate) {
                        requireActivity().finish()
                    }
                } else if (mustUpdate) requireActivity().finish()
            }

            onClose {
                if (mustUpdate) requireActivity().finish()
            }
        }
    }

    private fun openApk(apkFile: File) {
        val context = requireContext()
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val uri = FileProvider.getUriForFile(
                context.applicationContext,
                appConfig.fileProviderAuthority,
                apkFile,
            )
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            //GRANTING THE PERMISSIONS EXPLICITLY HERE! to all possible choosers (3rd party apps):
            val resolvedInfoActivities: List<ResolveInfo> = context.packageManager
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

            for (ri in resolvedInfoActivities) {
                context.grantUriPermission(
                    ri.activityInfo.packageName,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
        } else {
            val uri = Uri.fromFile(apkFile)
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
        }
        context.startActivity(
            Intent.createChooser(
                intent,
                context.resources.getText(R.string.share_receipt_to)
            ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
        )
    }

    private fun loadFcmToken(retryOnFail: Boolean = true) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("Support", "Fetching FCM registration token failed", task.exception)
                if (retryOnFail) {
                    loadFcmToken(retryOnFail = false)
                }
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


