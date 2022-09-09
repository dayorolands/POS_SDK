package com.cluster.screen.home

import android.content.SharedPreferences
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.cluster.R
import com.cluster.components.MenuButton
import com.cluster.core.config.InstitutionConfig
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.util.delegates.getArrayList
import com.cluster.utility.openPageById

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomerScreen(
    fragment: CreditClubFragment,
    institutionConfig: InstitutionConfig,
    preferences: SharedPreferences
) {
    val flows = institutionConfig.flows
    val returnedList = getArrayList("institution_features", preferences)

    SubMenu {
        if (returnedList != null) {
            if(returnedList.contains("ACC")) {
                item {
                    MenuButton(
                        text = stringResource(R.string.account_opening),
                        icon = painterResource(R.drawable.payday_loan),
                        onClick = { fragment.openPageById(R.id.register_button) }
                    )
                }
            }
        }
        if (flows.walletOpening != null) {
            item {
                MenuButton(
                    text = stringResource(R.string.title_activity_new_wallet),
                    icon = painterResource(R.drawable.payday_loan),
                    onClick = { fragment.openPageById(R.id.new_wallet_button) }
                )
            }
        }
        if(returnedList != null) {
            if(returnedList.contains("CBE")) {
                item {
                    MenuButton(
                        text = "Balance Enquiry",
                        icon = painterResource(R.drawable.income),
                        onClick = { fragment.openPageById(R.id.customer_balance_enquiry_button) }
                    )
                }
            }
        }
        if (flows.customerPinChange != null) {
            item {
                MenuButton(
                    text = "Change PIN",
                    icon = painterResource(R.drawable.login_password),
                    onClick = { fragment.openPageById(R.id.customer_change_pin_button) }
                )
            }
        }
        if (flows.bvnUpdate != null) {
            item {
                MenuButton(
                    text = "BVN Update",
                    icon = painterResource(R.drawable.secured_loan),
                    onClick = { fragment.openPageById(R.id.bvn_update_button) }
                )
            }
        }
    }
}