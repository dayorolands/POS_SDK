package com.cluster.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cluster.R

@Composable
fun SubMenuBottomNavigation(
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
                label = { Text(stringResource(id = screen.stringRes)) },
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
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

sealed class BottomNavScreens(
    val route: String,
    @StringRes val stringRes: Int,
    @DrawableRes val icon: Int
) {
    object Home : BottomNavScreens("Home", R.string.home, R.drawable.ic_home)
    object Customer : BottomNavScreens("Customer", R.string.customer, R.drawable.payday_loan)
    object Transactions :
        BottomNavScreens("Transactions", R.string.transactions, R.drawable.deposit)

    object Loans : BottomNavScreens("Loans", R.string.loans, R.drawable.personal_income)
    object Profile : BottomNavScreens("My Profile", R.string.profile, R.drawable.ic_person)
}