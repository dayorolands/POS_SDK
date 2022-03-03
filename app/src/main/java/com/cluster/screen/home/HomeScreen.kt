package com.cluster.screen.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.cluster.R
import com.cluster.Routes
import com.cluster.components.BottomNavScreens
import com.cluster.components.HomeAppBar
import com.cluster.components.SubMenuBottomNavigation
import com.cluster.components.currentRoute
import com.cluster.core.config.InstitutionConfig
import com.cluster.core.ui.CreditClubFragment
import com.cluster.ui.rememberBean
import com.cluster.utility.openPageById
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalStdlibApi::class)
@Composable
fun HomeScreen(
    mainNavController: NavController,
    composeNavController: NavHostController,
    fragment: CreditClubFragment,
) {
    val composableRouteFunctionIds = remember {
        mapOf(
            R.id.agent_change_pin_button to Routes.PinChange,
            R.id.funds_transfer_button to Routes.FundsTransfer,
            R.id.fn_support to Routes.SupportCases,
            R.id.ussd_withdrawal_button to Routes.UssdWithdrawal,
            R.id.fn_pending_transactions to Routes.PendingTransactions,
            R.id.fn_subscription to Routes.Subscription,
        )
    }
    val institutionConfig: InstitutionConfig by rememberBean()
    val homeNavController = rememberNavController()
    val scaffoldState = rememberScaffoldState()
    val bottomNavigationItems = remember {
        buildList {
            add(BottomNavScreens.Home)
            if (institutionConfig.categories.customers) {
                add(BottomNavScreens.Customer)
            }
            add(BottomNavScreens.Transactions)
            if (institutionConfig.categories.loans) {
                add(BottomNavScreens.Loans)
            }
            add(BottomNavScreens.Profile)
        }
    }
    val currentRoute = currentRoute(homeNavController)
    val greetingMessage = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Good Morning"
            in 12..15 -> "Good Afternoon"
            in 16..23 -> "Good Evening"
            else -> "Good Day"
        }
    }
    val title = when (currentRoute) {
        BottomNavScreens.Transactions.route -> stringResource(BottomNavScreens.Transactions.stringRes)
        BottomNavScreens.Customer.route -> stringResource(BottomNavScreens.Customer.stringRes)
        BottomNavScreens.Loans.route -> stringResource(BottomNavScreens.Loans.stringRes)
        BottomNavScreens.Profile.route -> ""
        else -> greetingMessage
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            HomeAppBar(
                mainNavController = mainNavController,
                title = title,
                navigationIcon = {
                    if (currentRoute != BottomNavScreens.Profile.route) {
                        IconButton(onClick = {
                            homeNavController.navigate(BottomNavScreens.Profile.route) {
                                launchSingleTop = true
                                popUpTo(BottomNavScreens.Home.route) {
                                    inclusive = false
                                }
                            }
                        }) {
                            Icon(
                                Icons.Outlined.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colors.primary.copy(0.52f)
                            )
                        }
                    }
                }
            )
        },
        bottomBar = { SubMenuBottomNavigation(homeNavController, bottomNavigationItems) },
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