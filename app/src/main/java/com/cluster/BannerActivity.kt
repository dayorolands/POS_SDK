package com.cluster

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.cluster.databinding.ActivityBannerBinding
import com.cluster.databinding.ItemBannerImageBinding
import com.creditclub.core.ui.SimpleBindingAdapter
import com.creditclub.core.util.delegates.jsonStore
import com.creditclub.core.util.safeRun
import com.creditclub.ui.dataBinding
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

class BannerActivity : AppCompatActivity(R.layout.activity_banner) {

    private val binding: ActivityBannerBinding by dataBinding()
    private val jsonPrefs by lazy { getSharedPreferences("JSON_STORAGE", 0) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnSkip.setOnClickListener { finish() }

        val bannerImageList: List<String>? by jsonPrefs.jsonStore(
            "DATA_BANNER_IMAGES",
            ListSerializer(String.serializer())
        )
        val data = bannerImageList ?: return finish()
        if (data.isEmpty()) return finish()

        binding.viewPager.adapter = SurveyAdapter(data)
        binding.indicator.setViewPager(binding.viewPager)
        binding.viewPager.adapter?.registerAdapterDataObserver(binding.indicator.adapterDataObserver)
    }

    inner class SurveyAdapter(override var values: List<String>) :
        SimpleBindingAdapter<String, ItemBannerImageBinding>(R.layout.item_banner_image) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            safeRun {
                holder.binding.image.load(values[position])
            }
        }
    }
}
