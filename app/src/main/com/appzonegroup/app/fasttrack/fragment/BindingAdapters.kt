package com.appzonegroup.app.fasttrack.fragment

import android.view.View
import android.widget.ArrayAdapter
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.appzonegroup.app.fasttrack.R
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.flow.StateFlow

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

    @JvmStatic
    @BindingAdapter("app:disableUnless")
    fun disableUnless(view: View, enable: Boolean) {
        view.isEnabled = enable
    }

    @JvmStatic
    @BindingAdapter("app:goneIf")
    fun goneIf(view: View, gone: Boolean) {
        view.visibility = if (gone) View.GONE else View.VISIBLE
    }

    @JvmStatic
    @BindingAdapter("app:showStartIconIfEmpty")
    fun showStartIconIfEmpty(textInputLayout: TextInputLayout, data: StateFlow<*>) {
        val value = data.value
        textInputLayout.isStartIconVisible = when (value) {
            is String? -> !value.isNullOrBlank()
            is Collection<*>? -> value == null || value.isEmpty()
            else -> data.value != null
        }
    }

    @JvmStatic
    @BindingAdapter("app:goneUnless")
    fun goneUnless(view: View, visible: Boolean) {
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("app:dependsOn")
    fun dependsOn(view: View, data: LiveData<String>) {
        view.isEnabled = !data.value.isNullOrBlank()
    }

    @JvmStatic
    @BindingAdapter("app:goneIfPresent")
    fun goneIfPresent(view: View, data: LiveData<*>) {
        val visible = when (data.value) {
            is String? -> (data.value as? String).isNullOrBlank()
            else -> data.value == null
        }
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("app:goneUnlessPresent")
    fun goneUnlessPresent(view: View, data: LiveData<*>) {
        val visible = when (data.value) {
            is String? -> !(data.value as? String).isNullOrBlank()
            else -> data.value != null
        }
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("app:goneUnlessPresent")
    fun goneUnlessPresent(view: View, data: StateFlow<*>) {
        val visible = when (data.value) {
            is String? -> !(data.value as? String).isNullOrBlank()
            else -> data.value != null
        }
        view.visibility = if (visible) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("app:showEndIconIfPresent")
    fun showEndIconIfPresent(textInputLayout: TextInputLayout, data: LiveData<*>) {
        val value = data.value
        textInputLayout.isEndIconVisible = when (value) {
            is String? -> !value.isNullOrBlank()
            is Boolean? -> value == true
            else -> data.value != null
        }
    }
}