package com.cluster.screen.cardlesswithdrawal

import android.content.SharedPreferences
import android.print.PrintJob
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SendToMobile
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.cluster.R
import com.cluster.core.config.InstitutionConfig
import com.cluster.core.data.model.Bank
import com.cluster.core.data.response.NameEnquiryResponse
import com.cluster.core.ui.CreditClubFragment
import com.cluster.core.util.delegates.getArrayList
import com.cluster.ui.CreditClubAppBar
import com.cluster.utility.openPageById
import kotlinx.coroutines.Job
import java.util.*
import kotlin.math.roundToInt

@Composable
fun CardlessWithdrawal(
    navController: NavController,
    fragment: CreditClubFragment,
    preferences: SharedPreferences
) {
    var isSameBank: Boolean? by rememberSaveable { mutableStateOf(null) }
    var bank: Bank? by remember(isSameBank) { mutableStateOf(null) }
    var receiverAccountNumber by remember(bank) { mutableStateOf("") }
    var nameEnquiryResponse: NameEnquiryResponse? by remember(bank, receiverAccountNumber) {
        mutableStateOf(null)
    }
    var receipt: PrintJob? by remember { mutableStateOf(null) }
    var activeJob: Job? by remember { mutableStateOf(null) }
    val returnedList = getArrayList("institution_features", preferences);
    val isVerified = nameEnquiryResponse != null
    var isTokenWithdrawal: Boolean? by rememberSaveable { mutableStateOf(null)}
    var selectType : Boolean? by rememberSaveable{ mutableStateOf(null) }
    var title = when{
        isSameBank == null -> "Cardless Withdrawal"
        selectType == null -> "Select Payment Mode"
        !isVerified -> stringResource(R.string.enter_account)
        isTokenWithdrawal == null -> "Token Withdrawal"
        else -> "Cardless Withdrawal"
    }

    Column(modifier = Modifier
        .background(
            colorResource(
                id = R.color.menuBackground
            )
        )
        .fillMaxSize()
    ) {
        CreditClubAppBar(
            title = title,
            onBackPressed = { navController.popBackStack() }

        )
        LazyColumn(modifier = Modifier.weight(1f)){
            if (isSameBank == null) {
                item(key = "select-type") {
                    Row(
                        modifier = Modifier
                            .padding(top = 10.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                    ) {
                        if(returnedList != null) {
                            if (returnedList.contains("TWT")) {
                                SmallMenuButton(
                                    text = stringResource(R.string.funds_transfer_same_bank),
                                    icon = R.drawable.funds_transfer_same_bank,
                                    onClick = { isSameBank = true },
                                    draw = true,
                                )
                            }
                            if(returnedList.contains("IBTW")) {
                                SmallMenuButton(
                                    text = stringResource(R.string.other_bank),
                                    icon = R.drawable.ic_bank_building,
                                    onClick = { isSameBank = false },
                                )
                            }
                        }
                    }
                }
            }

            if(selectType == null && isSameBank != null){
                item ( key = "Select-type" ){
                    Column(
                        modifier = Modifier
                            .padding(vertical = 20.dp, horizontal = 16.dp)
                            .fillMaxWidth(),
                    ){
                        ChipButton(
                            label = stringResource(id = R.string.withdraw_using_token),
                            onClick = { if(isSameBank == true) fragment.openPageById(R.id.token_withdrawal_button) else  fragment.openPageById(R.id.fn_token_withdrawal) },
                            imageVector = Icons.Outlined.SendToMobile
                        )
//                        Spacer(modifier = Modifier.padding(top = 10.dp))
//                        ChipButton(
//                            label = stringResource(id = R.string.withdraw_using_ussd),
//                            onClick = {
//                            },
//                            imageVector = Icons.Outlined.TransferWithinAStation
//                        )
//                        Spacer(modifier = Modifier.padding(top = 10.dp))
//                        ChipButton(
//                            label = stringResource(id = R.string.withdraw_using_mmo_wallet),
//                            onClick = {
//                            },
//                            imageVector = Icons.Outlined.PhoneAndroid
//                        )
//                        Spacer(modifier = Modifier.padding(top = 10.dp))
//                        ChipButton(
//                            label = stringResource(id = R.string.withdraw_using_qr_code),
//                            onClick = {
//                            },
//                            imageVector = Icons.Outlined.QrCode
//                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.SmallMenuButton(
    text: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    draw: Boolean = false,
    tint: Color = colorResource(R.color.colorAccent),
) {
    val context = LocalContext.current
    val drawable = remember(icon) { AppCompatResources.getDrawable(context, icon) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .heightIn(100.dp, 150.dp)
            .weight(1f)
            .padding(8.dp),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .weight(1f),
            elevation = 2.dp,
            shape = RoundedCornerShape(20.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.clickable(onClick = onClick),
            ) {
                if (draw) {
                    Box(
                        modifier = Modifier
                            .padding(30.dp)
                            .requiredSize(45.dp)
                            .drawBehind {
                                drawIntoCanvas { canvas ->
                                    drawable?.let {
                                        it.setBounds(
                                            0,
                                            0,
                                            size.width.roundToInt(),
                                            size.height.roundToInt()
                                        )
                                        it.draw(canvas.nativeCanvas)
                                    }
                                }
                            }
                    )
                } else {
                    Image(
                        painterResource(id = icon),
                        contentDescription = null,
                        alignment = Alignment.Center,
                        modifier = Modifier
                            .padding(30.dp)
                            .size(35.dp),
                        colorFilter = if (tint.alpha == 0f) null else ColorFilter.tint(tint),
                    )
                }
            }
        }

        Text(
            text = text.uppercase(Locale.ROOT),
            style = MaterialTheme.typography.button,
            color = colorResource(R.color.menuButtonTextColor),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ChipButton(
    label: String, 
    onClick: () -> Unit, 
    imageVector: ImageVector
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .border(
                BorderStroke(
                    1.dp,
                    MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                ),
                RoundedCornerShape(15.dp),
            )
    ) {
        Image(
            imageVector = imageVector,
            contentDescription = null,
            modifier = Modifier
                .padding(8.dp)
                .size(24.dp),
        )
        Text(
            text = label.uppercase(Locale.ROOT),
            color = colorResource(R.color.menuButtonTextColor),
            style = MaterialTheme.typography.caption,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 5.dp),
        )
    }
}