package com.creditclub.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun CreditClubTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = CreditClubColors,
        typography = CreditClubTypography,
        shapes = CreditClubShapes,
        content = content
    )
}
