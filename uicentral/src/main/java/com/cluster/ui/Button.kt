package com.cluster.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp

@Composable
inline fun AppButton(
    modifier: Modifier = Modifier,
    noinline onClick: () -> Unit,
    crossinline content: @Composable RowScope.() -> Unit
) {
    Button(
        modifier = Modifier
            .padding(16.dp)
            .height(50.dp)
            .fillMaxWidth()
            .then(modifier),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(R.color.colorAccent),
            contentColor = colorResource(R.color.colorAccentContrast),
        ),
        onClick = onClick
    ) {
        content()
    }
}

@Composable
inline fun AppUssdButton(
    modifier: Modifier = Modifier,
    noinline onClick: () -> Unit,
    crossinline content: @Composable RowScope.() -> Unit,
) {
    Button(
        modifier = Modifier
            .padding(16.dp)
            .height(50.dp)
            .fillMaxWidth()
            .then(modifier),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(R.color.colorAccent),
            contentColor = colorResource(R.color.colorAccentContrast),
        ),
        onClick = onClick
    ) {
        content()
    }
}

@Composable
inline fun AppPWTButton(
    modifier: Modifier = Modifier,
    noinline onClick: () -> Unit,
    crossinline content: @Composable RowScope.() -> Unit,
) {
    Button(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
            .height(50.dp)
            .fillMaxWidth()
            .then(modifier),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(R.color.darkGray),
            contentColor = colorResource(R.color.white),
        ),
        onClick = onClick
    ) {
        content()
    }
}

@Composable
inline fun AppPWTWhiteButton(
    modifier: Modifier = Modifier,
    noinline onClick: () -> Unit,
    crossinline content: @Composable RowScope.() -> Unit,
) {
    Button(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
            .height(50.dp)
            .fillMaxWidth()
            .then(modifier),
        shape = RoundedCornerShape(25.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(R.color.white),
            contentColor = colorResource(R.color.ef_grey),
        ),
        onClick = onClick
    ) {
        content()
    }
}

@Composable
inline fun AppSecondButton(
    modifier: Modifier = Modifier,
    noinline onClick: () -> Unit,
    crossinline content: @Composable RowScope.() -> Unit,
) {
    Button(
        modifier = Modifier
            .padding(16.dp)
            .height(50.dp)
            .fillMaxWidth()
            .then(modifier),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = colorResource(R.color.colorPrimary),
            contentColor = colorResource(R.color.colorAccentContrast),
        ),
        onClick = onClick
    ) {
        content()
    }
}

@Composable
inline fun AppTextButton(
    modifier: Modifier = Modifier,
    noinline onClick: () -> Unit,
    crossinline content: @Composable RowScope.() -> Unit,
) {
    val favModifier = Modifier
        .widthIn(max = 300.dp, min = 80.dp)
        .border(
            BorderStroke(
                1.dp,
                MaterialTheme.colors.onSurface.copy(alpha = 0.12f)
            ),
            RoundedCornerShape(15.dp),
        )
        .then(modifier)
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        modifier = favModifier
    ) {
        content()
    }
}