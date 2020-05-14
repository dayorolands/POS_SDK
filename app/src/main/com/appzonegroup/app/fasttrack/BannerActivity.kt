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
                "https://www.nairaland.com/attachments/4780893_firstdiasporasamplepost5_jpegedc0fafcbae1e363daa659878074460c",
                "https://pbs.twimg.com/media/C1QYfkAW8AE6Q3V.jpg",
                "https://www.bellanaija.com/wp-content/uploads/2016/09/instagram-1.jpg"
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
