package com.cluster.ui.adapter

import android.view.View
import com.cluster.core.ui.SimpleBindingAdapter
import com.cluster.core.ui.widget.DialogOptionItem
import com.cluster.ui.R
import com.cluster.ui.databinding.ItemDialogOptionBinding


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 25/09/2019.
 * Appzone Ltd
 */
class DialogOptionAdapter(override var values: List<DialogOptionItem>, val onItemClick: (Int)->Unit) :
    SimpleBindingAdapter<DialogOptionItem, ItemDialogOptionBinding>(R.layout.item_dialog_option) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        with(holder.binding) {
            titleTv.text = item.title

            if (item.subtitle != null) subtitleTv.text = item.subtitle
            else subtitleTv.visibility = View.GONE

            root.setOnClickListener {
                onItemClick(position)
            }
        }
    }
}