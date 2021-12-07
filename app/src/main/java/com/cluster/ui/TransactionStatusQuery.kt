package com.cluster.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cluster.R
import com.creditclub.ui.AppButton
import com.creditclub.ui.Loading

@Composable
fun TransactionStatusQuery(
    onClose: () -> Unit,
    title: String,
    message: String,
    imageVector: ImageVector = Icons.Outlined.HourglassTop,
    onRequery: (() -> Unit)? = null,
    loadingMessage: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.menuBackground)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(
                onClick = onClose,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (loadingMessage.isBlank()) {
                Icon(
                    modifier = Modifier
                        .size(80.dp),
                    imageVector = imageVector,
                    contentDescription = null,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.h4,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth(),
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                )
                Spacer(modifier = Modifier.padding(top = 30.dp))
                if (onRequery != null) {
                    AppButton(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        onClick = onRequery,
                    ) {
                        Text(text = "Check status")
                    }
                }
            } else {
                Loading(message = loadingMessage)
            }
        }
    }
}