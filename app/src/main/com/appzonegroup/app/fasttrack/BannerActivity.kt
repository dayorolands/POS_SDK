package com.appzonegroup.app.fasttrack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appzonegroup.app.fasttrack.databinding.ActivityBannerBinding
import com.appzonegroup.app.fasttrack.databinding.ItemBannerImageBinding
import com.creditclub.core.ui.SimpleBindingAdapter
import com.creditclub.core.util.delegates.contentView
import com.squareup.picasso.Picasso

class BannerActivity : AppCompatActivity() {

    private val binding by contentView<BannerActivity, ActivityBannerBinding>(R.layout.activity_banner)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnSkip.setOnClickListener { finish() }
        binding.viewPager.adapter = SurveyAdapter(
            listOf(
                "https://images.unsplash.com/photo-1574423466237-dd5717123e2b?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=600&q=80",
                "https://images.unsplash.com/photo-1574352935372-6b793d74ccd9?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=675&q=80",
                "https://images.unsplash.com/photo-1574342668504-21a158819842?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=634&q=80"
            )
        )
        binding.indicator.setViewPager(binding.viewPager)
        binding.viewPager.adapter?.registerAdapterDataObserver(binding.indicator.adapterDataObserver)
    }

    inner class SurveyAdapter(override var values: List<String>) :
        SimpleBindingAdapter<String, ItemBannerImageBinding>(R.layout.item_banner_image) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.binding) {
                Picasso.get().load(values[position]).into(image)
            }
        }
    }
}
