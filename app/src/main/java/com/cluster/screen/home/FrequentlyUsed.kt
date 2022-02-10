package com.cluster.screen.home


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cluster.R
import com.cluster.core.AppFunctions
import com.cluster.core.data.CoreDatabase
import com.cluster.core.data.model.AppFunctionUsage
import com.cluster.core.util.safeRunIO
import com.cluster.ui.rememberBean
import java.util.*

@Composable
fun FrequentlyUsed(onItemClick: (id: Int) -> Unit) {
    val coreDatabase: CoreDatabase by rememberBean()
    val frequentFunctions = produceState(emptyList<AppFunctionUsage>()) {
        val (list) = safeRunIO {
            coreDatabase.appFunctionUsageDao().getMostUsed()
        }
        value = list ?: emptyList()
    }
    if (frequentFunctions.value.isNotEmpty()) {
        Column {
            Text(
                text = "Frequently Used",
                color = colorResource(R.color.menuButtonTextColor),
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            val favModifier = Modifier
                .weight(1f)
                .widthIn(max = 300.dp, min = 80.dp)
                .padding(end = 10.dp)
                .border(
                    BorderStroke(
                        1.dp,
                        MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
                    ),
                    RoundedCornerShape(15.dp),
                )
            val menuButtonIconTint = colorResource(R.color.menuButtonIconTint)
            val tint = if (menuButtonIconTint.alpha == 0f) {
                LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
            } else {
                menuButtonIconTint
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 10.dp)
                    .wrapContentWidth()
            ) {
                frequentFunctions.value.forEach { appFunctionUsage ->
                    AppFunctions[appFunctionUsage.fid]?.run {
                        TextButton(
                            onClick = { onItemClick(id) },
                            shape = RoundedCornerShape(15.dp),
                            modifier = favModifier
                        ) {
                            if (icon != null) {
                                Icon(
                                    painter = painterResource(icon!!),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = tint,
                                )
                            }
                            Text(
                                text = stringResource(label).uppercase(Locale.ROOT),
                                color = colorResource(R.color.menuButtonTextColor),
                                style = MaterialTheme.typography.caption,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(start = 5.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}