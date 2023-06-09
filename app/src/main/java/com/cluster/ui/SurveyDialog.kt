package com.cluster.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.RatingBar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.cluster.R
import com.cluster.databinding.DialogSurveyBinding
import com.cluster.databinding.LayoutBooleanBinding
import com.cluster.databinding.LayoutMultipleChoiceBinding
import com.cluster.databinding.LayoutRatingBinding
import com.cluster.core.data.model.SurveyAnswer
import com.cluster.core.data.model.SurveyQuestion
import com.cluster.core.data.model.SurveyQuestionType
import com.cluster.core.ui.SimpleAdapter
import com.cluster.core.ui.widget.DialogListener
import com.cluster.core.ui.widget.DialogListenerBlock
import com.cluster.core.ui.widget.DialogOptionItem


class SurveyDialog private constructor(context: Context, questions: List<SurveyQuestion>) :
    Dialog(context) {

    val answers: MutableList<SurveyAnswer> = MutableList(questions.size) {
        SurveyAnswer()
    }

    var listener: DialogListenerBlock<List<SurveyAnswer>>? = null
        private set

    private var binding: DialogSurveyBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.dialog_survey,
        null,
        false
    )

    init {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCanceledOnTouchOutside(false)
        setCancelable(false)

        binding.viewPager.adapter = SurveyAdapter(questions)
        binding.indicator.setViewPager(binding.viewPager)
        binding.viewPager.adapter?.registerAdapterDataObserver(binding.indicator.adapterDataObserver)

        setContentView(binding.root)
        binding.btnClose.setOnClickListener {
            dismiss()
            if (listener != null) {
                DialogListener.create(listener!!).submit(this, answers)
            }
        }
        binding.btnSubmit.setOnClickListener {
            onNext()
        }
    }

    private fun onNext() {
        val viewPager = binding.viewPager
        if (viewPager.currentItem == viewPager.adapter?.itemCount?.minus(1) ?: 0) {
            DialogListener.create(listener!!).submit(this, answers)
            dismiss()
        } else viewPager.setCurrentItem(viewPager.currentItem + 1, true)
    }

    inner class SurveyAdapter(override var values: List<SurveyQuestion>) :
        SimpleAdapter<ViewHolder, SurveyQuestion>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurveyDialog.ViewHolder {
            val layoutRes = run {
                if (viewType == 1) R.layout.layout_multiple_choice
                if (viewType == 2) R.layout.layout_boolean
                else R.layout.layout_rating
            }

            val binding: ViewDataBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                layoutRes,
                parent,
                false
            )

            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SurveyDialog.ViewHolder, position: Int) {
            val question = values[position]
            val answer = answers[position]
            answer.questionId = question.id

            with(holder.binding) {
                when (this) {
                    is LayoutRatingBinding -> {
                        title = question.name
                        ratingBar.onRatingBarChangeListener =
                            RatingBar.OnRatingBarChangeListener { _, rating, _ ->
                                answer.rating = rating
                            }
                    }

                    is LayoutMultipleChoiceBinding -> {
                        val options =
                            question.options?.map { DialogOptionItem(it.text) } ?: emptyList()

                        title = question.name
                        list.layoutManager = LinearLayoutManager(context)
                        list.adapter = SurveyMultipleChoiceAdapter(options, onItemClick = {
                            answer.answerId = question.options!![it].id
                            onNext()
                        })
                    }

                    is LayoutBooleanBinding -> {
                        title = question.name

                        btnYes.setOnClickListener {
                            answer.answerId = "1"
                            onNext()
                        }

                        btnNo.setOnClickListener {
                            answer.answerId = "0"
                            onNext()
                        }
                    }
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when (values[position].type) {
                SurveyQuestionType.MultipleChoice -> 1
                SurveyQuestionType.Boolean -> 2
                else -> 0
            }
        }
    }

    inner class ViewHolder(val binding: ViewDataBinding) : SimpleAdapter.ViewHolder(binding.root)

    companion object {

        fun create(
            context: Context,
            questions: List<SurveyQuestion>,
            newListener: DialogListenerBlock<List<SurveyAnswer>>? = null
        ): SurveyDialog {
            return SurveyDialog(context, questions).apply { listener = newListener }
        }
    }
}
