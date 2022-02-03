package com.cluster.ui.theme

import androidx.compose.material.Colors
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import com.cluster.ui.R

/**
 * Return the fully opaque color that results from compositing [onSurface] atop [surface] with the
 * given [alpha]. Useful for situations where semi-transparent colors are undesirable.
 */
@Composable
fun Colors.compositedOnSurface(alpha: Float): Color {
    return onSurface.copy(alpha = alpha).compositeOver(surface)
}

val Red300 = Color(0xFFEA6D7E)

val CreditClubColors
    @Composable
    get() = lightColors(
        primary = colorResource(R.color.colorPrimary),
        onPrimary = Color.White,
        primaryVariant = colorResource(R.color.colorPrimaryDark),
        secondary = colorResource(R.color.colorAccent),
        onSecondary = colorResource(R.color.colorOnSecondary),
        error = Red300,
        onError = Color.Black
    )

/**
 * Calculates the color of an elevated `surface` in dark mode. Returns `surface` in light mode.
 */
@Composable
fun Colors.elevatedSurface(elevation: Dp): Color {
    return LocalElevationOverlay.current?.apply(
        color = this.surface,
        elevation = elevation
    ) ?: this.surface
}

@Composable
fun Colors.elevatedColor(color: Color, elevation: Dp): Color {
    return LocalElevationOverlay.current?.apply(
        color = color,
        elevation = elevation
    ) ?: color
}
