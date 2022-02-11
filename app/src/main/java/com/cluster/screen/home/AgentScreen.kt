package com.cluster.screen.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import com.cluster.R
import com.cluster.Routes
import com.cluster.components.MenuButton
import com.cluster.core.config.InstitutionConfig
import com.cluster.core.ui.CreditClubFragment
import com.cluster.utility.openPageById

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AgentScreen(
    fragment: CreditClubFragment,
    institutionConfig: InstitutionConfig,
    composeNavController: NavHostController
) {
    val flows = institutionConfig.flows

    SubMenu {
        item {
            MenuButton(
                text = "Mini Statement",
                icon = painterResource(R.drawable.deposit),
                onClick = { fragment.openPageById(R.id.agent_mini_statement_button) }
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
                    onClick = { fragment.openPageById(R.id.agent_balance_enquiry_button) }
                )
            }
        }
    }
}