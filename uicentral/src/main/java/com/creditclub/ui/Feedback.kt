package com.creditclub.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun Loading(message: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp)
            .then(modifier),
    ) {
        CircularProgressIndicator(modifier = Modifier.padding(bottom = 10.dp))
        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.onSurface.copy(0.52f),
        )
    }
}

@Composable
fun Loading(message: String, modifier: Modifier = Modifier, onCancel: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp)
            .then(modifier),
    ) {
        CircularProgressIndicator(modifier = Modifier.padding(bottom = 10.dp))
        Text(
            text = message,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.onSurface.copy(0.52f),
        )
        Spacer(modifier = Modifier.padding(top = 30.dp))
        AppButton(
            modifier = Modifier.padding(horizontal = 10.dp),
            onClick = onCancel,
        ) {
            Text(text = "Cancel")
        }
    }
}

@Composable
fun ErrorFeedback(errorMessage: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp)
            .then(modifier),
    ) {
        Image(
            painterResource(id = R.drawable.ic_sentiment_very_dissatisfied),
            contentDescription = null,
            modifier = Modifier
                .padding(bottom = 10.dp)
                .size(100.dp),
            colorFilter = ColorFilter.tint(colorResource(R.color.colorAccent))
        )
        Text(
            text = errorMessage,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.onSurface.copy(0.52f),
        )
    }
}

@Composable
fun ErrorMessage(content: String) {
    if (content.isBlank()) return
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp)
    ) {
        Image(
            painterResource(id = R.drawable.ic_sentiment_very_dissatisfied),
            contentDescription = null,
            modifier = Modifier
                .padding(end = 10.dp)
                .size(24.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface.copy(0.52f))
        )
        Text(
            text = content,
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.onSurface.copy(0.52f),
        )
    }
}
