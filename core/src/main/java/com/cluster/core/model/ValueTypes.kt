package com.cluster.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IntValueType(val value: Int, val label: String) : Parcelable {
    override fun toString() = label
}