package com.cluster.ui

import com.cluster.R
import com.cluster.databinding.ItemSurveyChoiceBinding
import com.cluster.core.ui.SimpleBindingAdapter
import com.cluster.core.ui.widget.DialogOptionItem


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 18/11/2019.
 * Appzone Ltd
 */

class SurveyMultipleChoiceAdapter(
    override var values: List<DialogOptionItem>,
    val onItemClick: (Int) -> Unit
) :
    SimpleBindingAdapter<DialogOptionItem, ItemSurveyChoiceBinding>(R.layout.item_survey_choice) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        with(holder.binding) {
            titleTv.text = item.title

            root.setOnClickListener {
                onItemClick(position)
            }
        }
    }
}