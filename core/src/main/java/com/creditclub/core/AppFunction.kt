package com.creditclub.core

import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes


data class AppFunction(@IdRes val id: Int, @StringRes val label: Int, @DrawableRes val icon: Int? = null)