package com.creditclub.core.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class SimpleBindingAdapter<T, out V : ViewDataBinding>(@LayoutRes private val itemLayout: Int) :
    SimpleAdapter<SimpleBindingAdapter<T, V>.ViewHolder, T>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<V>(LayoutInflater.from(parent.context), itemLayout, parent, false)

        return ViewHolder(binding)
    }

    inner class ViewHolder(val binding: V) : SimpleAdapter.ViewHolder(binding.root)
}
