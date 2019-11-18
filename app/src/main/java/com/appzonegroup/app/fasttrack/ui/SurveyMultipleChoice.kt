package com.appzonegroup.app.fasttrack.ui

import com.creditclub.core.ui.SimpleBindingAdapter
import com.creditclub.core.ui.widget.DialogOptionItem
import com.creditclub.ui.R
import com.creditclub.ui.databinding.ItemDialogOptionBinding


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 18/11/2019.
 * Appzone Ltd
 */

class SurveyMultipleChoiceAdapter(
    override var values: List<DialogOptionItem>,
    val onItemClick: (Int) -> Unit
) :
    SimpleBindingAdapter<DialogOptionItem, ItemDialogOptionBinding>(R.layout.item_dialog_option) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        with(holder.binding) {
            titleTv.text = item.title

            if (item.subtitle != null) subtitleTv.text = item.subtitle
            else subtitleTv.visibility = android.view.View.GONE

            root.setOnClickListener {
                onItemClick(position)
            }
        }
    }
}