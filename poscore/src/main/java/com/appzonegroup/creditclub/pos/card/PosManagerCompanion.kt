package com.appzonegroup.creditclub.pos.card

import android.content.Context
import org.koin.core.module.Module

interface PosManagerCompanion {
    val module: Module
    fun isCompatible(): Boolean
    fun setup(context: Context): Boolean
}