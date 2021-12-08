package com.cluster.core.ui

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class SimpleAdapter<A : RecyclerView.ViewHolder, T> : RecyclerView.Adapter<A>() {

    protected abstract var values: List<T>

    fun setData(newData: List<T>?) {
        this.values = newData ?: emptyList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = values.size

    abstract class ViewHolder(val rootView: View) : RecyclerView.ViewHolder(rootView)
}
