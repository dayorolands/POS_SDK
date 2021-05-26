@file:JvmName("ViewExt")

package com.creditclub.core.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

inline fun <reified T : ViewDataBinding> LayoutInflater.dataBinding(
    @LayoutRes layout: Int,
    parent: ViewGroup? = null,
    attachToParent: Boolean = false
): T {
    return DataBindingUtil.inflate(this, layout, parent, attachToParent)
}