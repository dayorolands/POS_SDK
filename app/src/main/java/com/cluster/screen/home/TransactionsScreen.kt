package com.cluster.screen.home

import android.content.SharedPreferences
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cluster.R
import com.cluster.Routes
import com.cluster.components.SmallMenuButton
import com.cluster.core.config.InstitutionConfig
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.util.delegates.getArrayList
import com.cluster.pos.Platform
import com.cluster.utility.openPageById

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    fragment: CreditClubFragment,
    institutionConfig: InstitutionConfig,
    composeNavController: NavController,
    preferences: SharedPreferences
) {

    LazyVerticalGrid(
        cells = GridCells.Adaptive(minSize = 100.dp)
    ) {
        if (Platform.isPOS) {
            item {
                SmallMenuButton(
                    text = "Card Transactions",
                    icon = painterResource(R.drawable.withdraw),
                    onClick = { fragment.openPageById(R.id.card_withdrawal_button) }
                )
            }
        }
    }
}