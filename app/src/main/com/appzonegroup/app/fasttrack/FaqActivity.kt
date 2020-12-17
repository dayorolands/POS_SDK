package com.appzonegroup.app.fasttrack

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.appzonegroup.app.fasttrack.databinding.ActivityFaqBinding
import com.appzonegroup.app.fasttrack.databinding.ListItemFaqAnswerBinding
import com.appzonegroup.app.fasttrack.databinding.ListItemFaqQuestionBinding
import com.appzonegroup.app.fasttrack.utility.FunctionIds
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
        supportActionBar?.title = "FAQ"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.list.layoutManager = LinearLayoutManager(this)
        mainScope.launch {
            val (faqs) = safeRunIO {
                val fileContents = assets.open("faq.json").rawContents
                Json.decodeFromString(FaqResponse.serializer(), fileContents)
            }

            faqs ?: return@launch
            binding.list.adapter = GenreAdapter(faqs.data?.map { FaqGroup(it) } ?: emptyList())
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
            childIndex: Int
        ) {
            val answer = (group as FaqGroup).items[childIndex]
            holder.binding.answerText = answer.value
        }

        override fun onBindGroupViewHolder(
            holder: QuestionViewHolder,
            flatPosition: Int,
            group: ExpandableGroup<*>
        ) {
            holder.binding.questionText = (group as FaqGroup).faqItem.question
        }
    }


    private class QuestionViewHolder(val binding: ListItemFaqQuestionBinding) :
        GroupViewHolder(binding.root)

    private class AnswerViewHolder(val binding: ListItemFaqAnswerBinding) :
        ChildViewHolder(binding.root)

    private class FaqGroup(val faqItem: FaqItem) : ExpandableGroup<StringParcel>(
        faqItem.question, listOf(
            StringParcel(faqItem.answer)
        )
    )

    private class StringParcel(val value: String?) : Parcelable {
        constructor(parcel: Parcel) : this(parcel.readString())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(value)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<StringParcel> {
            override fun createFromParcel(parcel: Parcel): StringParcel {
                return StringParcel(parcel)
            }

            override fun newArray(size: Int): Array<StringParcel?> {
                return arrayOfNulls(size)
            }
        }
    }
}
