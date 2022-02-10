package com.cluster.screen.home


import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cluster.*
import com.cluster.R
import com.cluster.activity.UpdateActivity
import com.cluster.core.config.InstitutionConfig
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.pos.Platform
import com.cluster.ui.rememberBean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun HomeDrawerContent(
    scaffoldState: ScaffoldState,
    coroutineScope: CoroutineScope,
    openPage: (Int) -> Unit,
) {
    val localStorage: LocalStorage by rememberBean()
    val institutionConfig: InstitutionConfig by rememberBean()
    val agent = localStorage.agent
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.primary.copy(0.2f))
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painter = painterResource(R.drawable.ic_launcher_transparent),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .padding(16.dp),
        )
        Text(
            text = agent?.agentName ?: "",
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp),
            softWrap = true,
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.onSurface,
        )
        Text(
            text = agent?.phoneNumber ?: "",
            modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface.copy(0.5f),
        )
    }

    if (institutionConfig.hasOnlineFunctions) {
        DrawerRow(
            title = stringResource(R.string.online_functions),
            imageVector = Icons.Outlined.ArrowUpward,
            onClick = {
                coroutineScope.launch { scaffoldState.drawerState.close() }
                context.startActivity(Intent(context, OnlineActivity::class.java))
            }
        )
    }

    DrawerRow(
        title = stringResource(R.string.reports),
        imageVector = Icons.Outlined.ReceiptLong,
        onClick = {
            coroutineScope.launch { scaffoldState.drawerState.close() }
            context.startActivity(Intent(context, ReportActivity::class.java))
        }
    )

    DrawerRow(
        title = stringResource(R.string.commission),
        imageVector = Icons.Outlined.Payments,
        onClick = {
            coroutineScope.launch { scaffoldState.drawerState.close() }
            context.startActivity(Intent(context, CommissionsActivity::class.java))
        }
    )

    DrawerRow(
        title = stringResource(R.string.pending_transactions),
        imageVector = Icons.Outlined.HourglassEmpty,
        onClick = {
            coroutineScope.launch {
                scaffoldState.drawerState.close()
                openPage(R.id.fn_pending_transactions)
            }
        }
    )

    DrawerRow(
        title = stringResource(R.string.support),
        imageVector = Icons.Outlined.ChatBubbleOutline,
        onClick = {
            coroutineScope.launch {
                scaffoldState.drawerState.close()
                openPage(R.id.fn_support)
            }
        }
    )

    if (institutionConfig.hasHlaTagging) {
        DrawerRow(
            title = stringResource(R.string.hla_tagging),
            imageVector = Icons.Outlined.Place,
            onClick = {
                coroutineScope.launch { scaffoldState.drawerState.close() }
                context.startActivity(Intent(context, HlaTaggingActivity::class.java))
            }
        )
    }

    DrawerRow(
        title = stringResource(R.string.title_activity_faq),
        imageVector = Icons.Outlined.HelpOutline,
        onClick = {
            coroutineScope.launch { scaffoldState.drawerState.close() }
            context.startActivity(Intent(context, FaqActivity::class.java))
        }
    )

    if (Platform.isPOS && Platform.deviceType != 2) {
        DrawerRow(
            title = stringResource(R.string.update),
            imageVector = Icons.Outlined.ArrowDownward,
            onClick = {
                coroutineScope.launch { scaffoldState.drawerState.close() }
                context.startActivity(Intent(context, UpdateActivity::class.java))
            }
        )
    }
}

@Composable
private fun DrawerRow(
    title: String,
    image: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val background = Color.Transparent
    val textColor = MaterialTheme.colors.onSurface.copy(0.5f)
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(background)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        image()
        Text(color = textColor, text = title)
    }
}

@Composable
private fun DrawerRow(
    title: String,
    imageVector: ImageVector,
    onClick: () -> Unit
) {
    val textColor = MaterialTheme.colors.onSurface.copy(0.5f)
    DrawerRow(
        title = title,
        image = {
            Image(
                imageVector = imageVector,
                contentDescription = null,
                colorFilter = ColorFilter.tint(textColor),
                modifier = Modifier
                    .padding(16.dp)
                    .size(24.dp),
            )
        },
        onClick = onClick,
    )
}