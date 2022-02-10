package com.cluster.screen.home

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.cluster.R
import com.cluster.Routes
import com.cluster.components.*
import com.cluster.core.config.InstitutionConfig
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.util.debugOnly
import com.cluster.core.util.packageInfo
import com.cluster.ui.rememberBean
import com.cluster.utility.logout
import com.cluster.utility.openPageById
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalStdlibApi::class)
@Composable
fun HomeScreen(
    mainNavController: NavController,
    composeNavController: NavHostController,
    activity: Activity,
    fragment: CreditClubFragment,
) {
    val composableRouteFunctionIds = remember {
        mapOf(
            R.id.agent_change_pin_button to Routes.PinChange,
            R.id.funds_transfer_button to Routes.FundsTransfer,
            R.id.fn_support to Routes.SupportCases,
            R.id.ussd_withdrawal_button to Routes.UssdWithdrawal,
            R.id.fn_pending_transactions to Routes.PendingTransactions,
        )
    }
    val institutionConfig: InstitutionConfig by rememberBean()
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
                    HomeDrawerContent(
                        scaffoldState = scaffoldState,
                        coroutineScope = coroutineScope,
                        openPage = {
                            if (composableRouteFunctionIds.containsKey(it)) {
                                composeNavController.navigate(composableRouteFunctionIds[it]!!)
                            } else {
                                fragment.openPageById(it)
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
                    .clickable { activity.logout() },
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
                    homeRoutes(
                        institutionConfig = institutionConfig,
                        homeNavController = homeNavController,
                        composeNavController = composeNavController,
                        fragment = fragment,
                    )
                }
            }

            if (currentRoute == BottomNavScreens.Home.route) {
                FrequentlyUsed(onItemClick = {
                    if (composableRouteFunctionIds.containsKey(it)) {
                        composeNavController.navigate(composableRouteFunctionIds[it]!!)
                    } else {
                        fragment.openPageById(it)
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