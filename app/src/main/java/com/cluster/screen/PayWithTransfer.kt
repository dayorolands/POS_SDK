package com.cluster.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.cluster.R
import com.cluster.ui.CreditClubAppBar

@Composable
fun PayWithTransfer(
    navController: NavController
){


    Column() {
        CreditClubAppBar(
            title = stringResource(id = R.string.pay_with_transfer),
            onBackPressed = {
                navController.popBackStack()
            }
        )

        LazyColumn(){

        }
    }
}