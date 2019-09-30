package com.appzonegroup.app.fasttrack.ui

import android.content.Context
import androidx.databinding.DataBindingUtil
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.SpinnerAdapter
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.LayoutSpinnerBinding

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/9/2019.
 * Appzone Ltd
 */

class Picker : LinearLayout {
    private lateinit var binding: LayoutSpinnerBinding

    var label
        get() = binding.label
        set(value) {
            binding.label = value
        }

    var adapter: SpinnerAdapter
        get() = binding.spinner.adapter
        set(value) {
            binding.spinner.adapter = value
        }

    val selectedItemPosition
        get() = binding.spinner.selectedItemPosition

    val selectedItem: Any?
        get() = binding.spinner.selectedItem

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet? = null) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.layout_spinner, this, true)
        attrs?.run {
            val a = context.obtainStyledAttributes(attrs, R.styleable.Picker)
            a.getString(R.styleable.Picker_label)?.let { label = it }
            a.recycle()
        }
    }
}