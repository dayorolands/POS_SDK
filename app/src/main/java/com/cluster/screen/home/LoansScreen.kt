package com.cluster.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.cluster.R
import com.cluster.components.MenuButton
import com.cluster.core.ui.CreditClubFragment
import com.cluster.utility.openPageById

@Composable
fun LoansScreen(fragment: CreditClubFragment) {
    SubMenu {
        item {
            MenuButton(
                text = "Loan Request",
                icon = painterResource(R.drawable.personal_income),
                onClick = { fragment.openPageById(R.id.loan_request_button) }
            )
        }
    }
}