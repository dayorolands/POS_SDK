package com.creditclub.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appzonegroup.app.fasttrack.R
import java.util.*

@Composable
fun MenuButton(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
) {
    val menuButtonIconTint = colorResource(R.color.menuButtonIconTint)
    Card(
        modifier = Modifier
            .heightIn(150.dp, 200.dp)
            .widthIn(150.dp, 300.dp)
            .padding(10.dp),
        elevation = 2.dp,
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.clickable(onClick = onClick),
        ) {
            Image(
                icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .size(35.dp),
                colorFilter = if (menuButtonIconTint.alpha == 0f) null else ColorFilter.tint(
                    menuButtonIconTint
                )
            )
            Text(
                text = text.uppercase(Locale.ROOT),
                style = MaterialTheme.typography.button,
                color = colorResource(R.color.menuButtonTextColor),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(10.dp),
            )
        }
    }
}

@Composable
fun SmallMenuButton(
    text: String,
    icon: Painter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val menuButtonIconTint = colorResource(R.color.menuButtonIconTint)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .heightIn(100.dp, 150.dp)
            .widthIn(100.dp, 120.dp)
            .padding(8.dp)
            .then(modifier),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .weight(1f)
                .clickable(onClick = onClick),
            elevation = 2.dp,
            shape = RoundedCornerShape(20.dp),
        ) {
            Image(
                icon,
                contentDescription = null,
                alignment = Alignment.Center,
                modifier = Modifier
                    .padding(30.dp)
                    .size(35.dp),
                colorFilter = if (menuButtonIconTint.alpha == 0f) null else ColorFilter.tint(
                    menuButtonIconTint
                )
            )
        }

        Column(modifier = Modifier.height(20.dp)) {
            Text(
                text = text.uppercase(Locale.ROOT),
                style = MaterialTheme.typography.button,
                fontSize = 12.sp,
                color = colorResource(R.color.menuButtonTextColor),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}