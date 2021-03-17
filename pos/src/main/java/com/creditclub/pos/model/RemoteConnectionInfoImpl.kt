package com.creditclub.pos.model

import android.os.Parcelable
import com.creditclub.pos.RemoteConnectionInfo
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class RemoteConnectionInfoImpl(
    override val id: String,
    override val label: String,
    override val key1: String,
    override val key2: String,
    override val ip: String,
    override val port: Int,
    override val ssl: Boolean = true,
    override val dukptConfig: DukptConfigImpl? = null,
    override val timeout: Int = 60,
    override val nodeName: String? = null,
    override val requeryConfig: RequeryConfigImpl? = null,
) : RemoteConnectionInfo, Parcelable