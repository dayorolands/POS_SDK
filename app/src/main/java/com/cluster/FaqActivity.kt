package com.cluster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.cluster.databinding.ActivityFaqBinding
import com.cluster.databinding.ListItemFaqAnswerBinding
import com.cluster.databinding.ListItemFaqQuestionBinding
import com.cluster.utility.FunctionIds
import com.creditclub.core.data.StringParcel
import com.creditclub.core.data.model.FaqItem
import com.creditclub.core.data.response.FaqResponse
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.util.rawContents
import com.creditclub.core.util.safeRunIO
import com.creditclub.ui.dataBinding
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class FaqActivity : CreditClubActivity(R.layout.activity_faq) {
    override val functionId = FunctionIds.FAQS
    private val binding: ActivityFaqBinding by dataBinding()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.run {
            title = "FAQ"
            setDisplayHomeAsUpEnabled(true)
        }

        binding.list.layoutManager = LinearLayoutManager(this)
        mainScope.launch {
            val (faqs) = safeRunIO {
                assets.open("faq.json").use { inputStream ->
                    val fileContents = inputStream.rawContents
                    Json.decodeFromString(FaqResponse.serializer(), fileContents)
                }
            }

            if (faqs == null) return@launch
            binding.list.adapter = GenreAdapter(faqs.data?.map { FaqGroup(it) } ?: emptyList())
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private class GenreAdapter(groups: List<ExpandableGroup<*>>) :
        ExpandableRecyclerViewAdapter<QuestionViewHolder, AnswerViewHolder>(groups) {

        override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
            val binding: ListItemFaqQuestionBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.list_item_faq_question,
                parent,
                false
            )
            return QuestionViewHolder(binding)
        }

        override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
            val binding: ListItemFaqAnswerBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.list_item_faq_answer,
                parent,
                false
            )
            return AnswerViewHolder(binding)
        }

        override fun onBindChildViewHolder(
            holder: AnswerViewHolder, flatPosition: Int, group: ExpandableGroup<*>,
            childIndex: Int,
        ) {
            val answer = (group as FaqGroup).items[childIndex]
            holder.binding.answerText = answer.value
        }

        override fun onBindGroupViewHolder(
            holder: QuestionViewHolder,
            flatPosition: Int,
            group: ExpandableGroup<*>,
        ) {
            holder.binding.questionText = (group as FaqGroup).faqItem.question
        }
    }


    private class QuestionViewHolder(val binding: ListItemFaqQuestionBinding) :
        GroupViewHolder(binding.root)

    private class AnswerViewHolder(val binding: ListItemFaqAnswerBinding) :
        ChildViewHolder(binding.root)

    private class FaqGroup(val faqItem: FaqItem) : ExpandableGroup<StringParcel>(
        faqItem.question, listOf(StringParcel(faqItem.answer))
    )
}
