package com.appzonegroup.app.fasttrack.fragment

import android.view.View
import android.widget.ArrayAdapter
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import com.appzonegroup.app.fasttrack.R
import com.google.android.material.textfield.MaterialAutoCompleteTextView

object BindingAdapters {
    @JvmStatic
    @BindingAdapter("app:dependsOn")
    fun View.dependsOn(data: Any?) {
        isEnabled = when (data) {
            is String? -> !data.isNullOrBlank()
            else -> data != null
        }
    }

    @JvmStatic
    @BindingAdapter("app:suggestions")
    fun <T> MaterialAutoCompleteTextView.suggestions(suggestions: List<T>?) {
        val items = suggestions ?: emptyList()
        val adapter = ArrayAdapter(context, R.layout.list_item, items)
        setAdapter(adapter)
    }

    @JvmStatic
    @BindingAdapter("app:selected")
    @Suppress("UNCHECKED_CAST")
    fun <T> MaterialAutoCompleteTextView.selected(item: MutableLiveData<T>) {
        setOnItemClickListener { parent, _, position, _ ->
            item.postValue(parent.getItemAtPosition(position) as T)
        }
    }
}