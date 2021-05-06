package com.creditclub.ui

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp

@Composable
inline fun AppButton(
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