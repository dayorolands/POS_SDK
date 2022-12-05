package com.cluster.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.cluster.R
import com.cluster.Routes
import com.cluster.components.HomeAppBar
import com.cluster.core.util.toCurrencyFormat
import com.cluster.ui.AppButton
import com.cluster.ui.AppPWTButton
import com.cluster.ui.AppPWTWhiteButton
import com.cluster.ui.CreditClubAppBar
import com.cluster.ui.theme.CreditClubTheme
import kotlinx.coroutines.launch

/**
 * Created by Ifedayo Adekoya <iadekoya@appzonegroup.com> on 29/11/2022.
 * Appzone Ltd
 */

@Composable
fun PayWithTransferDetails(
    amount: Int?,
    virtualAccountNo : String?,
    navController: NavController
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    Column(
            modifier = Modifier.fillMaxSize()
    ){
        CreditClubAppBar(
            title = stringResource(id = R.string.pay_with_transfer),
            onBackPressed = {
                navController.popBackStack()
            }
        )
        LazyColumn {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .height(420.dp)
                        .fillMaxWidth()
                        .padding(15.dp),
                    backgroundColor = colorResource(id = R.color.ef_grey),
                    elevation= 5.dp,
                    contentColor = colorResource(id = R.color.black)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Transfer exactly",
                            textAlign = TextAlign.Center,
                            fontSize = 25.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = amount?.toCurrencyFormat()!!,
                            fontSize = 25.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "TO",
                            textAlign = TextAlign.Center,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = stringResource(id = R.string.institution_name),
                            textAlign = TextAlign.Center,
                            fontSize = 25.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = virtualAccountNo.toString(),
                            textAlign = TextAlign.Center,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "This Account number is only valid for 30 minutes",
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth()
                                .align(Alignment.CenterHorizontally)
                                .padding(start = 35.dp, end = 35.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Card(
                            modifier = Modifier
                                .height(40.dp)
                                .fillMaxWidth()
                                .padding(start = 40.dp, end = 40.dp)
                                .clickable(onClick = {
                                    coroutineScope.launch {
                                        val clipboardManager =
                                            context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                                        val clipData: ClipData = ClipData.newPlainText(
                                            "Account Number",
                                            virtualAccountNo
                                        )
                                        clipboardManager.setPrimaryClip(clipData)
                                        Toast
                                            .makeText(
                                                context,
                                                "Account number has been copied",
                                                Toast.LENGTH_LONG
                                            )
                                            .show()
                                    }

                                }),
                            elevation = 5.dp,
                            backgroundColor = colorResource(R.color.black4),
                            contentColor = colorResource(id = R.color.black),
                            shape = RoundedCornerShape(20.dp),
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 5.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Copy Account Number",
                                        fontSize = 12.sp,
                                        style = MaterialTheme.typography.button,
                                        color = colorResource(id = R.color.black),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .padding(horizontal = 10.dp)
                                            .weight(1f),
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.FileCopy,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        AppPWTButton(
            onClick = {
                coroutineScope.launch {

                }
            }
        ) {
            Text(
                text = stringResource(id = R.string.confirmPayment))
        }
        Spacer(modifier = Modifier.height(8.dp))
        AppPWTWhiteButton(
            onClick = {
                navController.navigate(Routes.PayWithTransfer)
            }
        ) {
            Text(
                text = stringResource(id = R.string.another_payment)
            )
        }
    }
}

const val amountVar = 1000

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    CreditClubTheme {
        PayWithTransferDetails(
            navController = rememberNavController(),
            amount = amountVar,
            virtualAccountNo = "102345678"
        )
    }
}