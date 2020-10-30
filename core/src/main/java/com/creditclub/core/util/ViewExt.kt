@file:JvmName("ViewExt")

package com.creditclub.core.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.viewbinding.ViewBinding

inline fun <reified T : ViewBinding> LayoutInflater.viewBinding(
    viewBindingFactory: (View) -> T,
    @LayoutRes layout: Int,
    parent: ViewGroup? = null,
    attachToParent: Boolean = false
): T {
    val view = inflate(layout, parent, attachToParent)
    return viewBindingFactory(view)
}

inline fun <reified T : ViewDataBinding> LayoutInflater.dataBinding(
    @LayoutRes layout: Int,
    parent: ViewGroup? = null,
    attachToParent: Boolean = false
): T {
    return DataBindingUtil.inflate(this, layout, parent, attachToParent)
}