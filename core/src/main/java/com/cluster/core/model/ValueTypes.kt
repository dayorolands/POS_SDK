package com.cluster.core.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IntValueType(val value: Int, val label: String) : Parcelable {
    override fun toString() = label
}

@Parcelize
data class BooleanValueType(val value: Boolean, val label: String) : Parcelable {
    override fun toString() = label
}