package com.cluster.core.util.delegates

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import kotlin.reflect.KProperty

/**
 * A delegate who lazily inflates a data binding layout, calls [Activity.setContentView] and returns
 * the binding.
 */
class ContentViewBindingDelegate<in R : ComponentActivity, out T : ViewDataBinding>(
    @LayoutRes private val layoutRes: Int
) {

    private var binding: T? = null

    operator fun getValue(activity: R, property: KProperty<*>): T {
        if (binding == null) {
            binding = DataBindingUtil.setContentView<T>(activity, layoutRes).also {
                it.lifecycleOwner = activity
            }
        }
        return binding!!
    }
}

fun <R : ComponentActivity, T : ViewDataBinding> contentView(
    @LayoutRes layoutRes: Int
): ContentViewBindingDelegate<R, T> = ContentViewBindingDelegate(layoutRes)
