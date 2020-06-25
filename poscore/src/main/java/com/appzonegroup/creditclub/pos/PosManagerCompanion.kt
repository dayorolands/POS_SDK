package com.appzonegroup.creditclub.pos

import android.content.Context
import org.koin.core.module.Module

interface PosManagerCompanion {
    val module: Module
    fun isCompatible(context: Context): Boolean
    fun setup(context: Context)
}