package com.cluster.adapter.online

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.cluster.R
import com.cluster.model.online.Option

class OptionsAdapter(
    private var ctx: Context,
    private var res: Int,
    private var data: List<Option>,
) : ArrayAdapter<Option>(ctx, res, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var v = convertView
        if (v == null) {
            v = LayoutInflater.from(ctx).inflate(res, parent, false)
        }
        val vh = ViewHolder(v!!)
        val option = getItem(position)
        vh.optionName.text = option.name
        return v
    }

    override fun getItem(position: Int): Option {
        return data[position]
    }

    private inner class ViewHolder(v: View) {
        var optionName: TextView = v.findViewById(R.id.optionName)
    }
}