package com.appzonegroup.app.fasttrack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appzonegroup.app.fasttrack.databinding.ActivityBannerBinding
import com.appzonegroup.app.fasttrack.databinding.ItemBannerImageBinding
import com.creditclub.core.data.prefs.JsonStorage
import com.creditclub.core.ui.SimpleBindingAdapter
import com.creditclub.core.util.delegates.contentView
import com.creditclub.core.util.safeRun
import com.squareup.picasso.Picasso

class BannerActivity : AppCompatActivity() {

    private val binding by contentView<BannerActivity, ActivityBannerBinding>(R.layout.activity_banner)
    private val jsonStore by lazy { JsonStorage.getStore(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnSkip.setOnClickListener { finish() }

        val bannerImageJson = jsonStore.get<List<String>>("BANNER_IMAGES")
        val bannerImageList = bannerImageJson.data ?: return finish()
        if (bannerImageList.isNullOrEmpty()) return finish()

        binding.viewPager.adapter = SurveyAdapter(bannerImageList)
        binding.indicator.setViewPager(binding.viewPager)
        binding.viewPager.adapter?.registerAdapterDataObserver(binding.indicator.adapterDataObserver)
    }

    inner class SurveyAdapter(override var values: List<String>) :
        SimpleBindingAdapter<String, ItemBannerImageBinding>(R.layout.item_banner_image) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            with(holder.binding) {
                safeRun {
                    Picasso.get().load(values[position]).into(image)
                }
            }
        }
    }
}
