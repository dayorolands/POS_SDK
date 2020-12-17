package com.appzonegroup.app.fasttrack

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.appzonegroup.app.fasttrack.databinding.ActivityBannerBinding
import com.appzonegroup.app.fasttrack.databinding.ItemBannerImageBinding
import com.creditclub.core.ui.SimpleBindingAdapter
import com.creditclub.core.util.delegates.jsonStore
import com.creditclub.core.util.safeRun
import com.creditclub.ui.dataBinding
import com.squareup.picasso.Picasso

class BannerActivity : AppCompatActivity(R.layout.activity_banner) {

    private val binding: ActivityBannerBinding by dataBinding()
    private val jsonPrefs by lazy { getSharedPreferences("JSON_STORAGE", 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnSkip.setOnClickListener { finish() }

        val bannerImageList by jsonPrefs.jsonStore<List<String>>("DATA_BANNER_IMAGES")
        val data = bannerImageList ?: return finish()
        if (data.isEmpty()) return finish()

        binding.viewPager.adapter = SurveyAdapter(data)
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
