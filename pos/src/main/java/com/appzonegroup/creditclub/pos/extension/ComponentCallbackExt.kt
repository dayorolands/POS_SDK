package com.appzonegroup.creditclub.pos.extension

import android.content.ComponentCallbacks
import com.creditclub.pos.PosConfig
import com.creditclub.pos.PosParameter
import org.koin.android.ext.android.get

inline val ComponentCallbacks.posParameter get() = get<PosParameter>()
inline val ComponentCallbacks.posConfig get() = get<PosConfig>()
