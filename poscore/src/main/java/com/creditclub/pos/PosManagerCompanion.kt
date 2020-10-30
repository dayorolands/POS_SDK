package com.creditclub.pos

import android.content.Context
import org.koin.core.module.Module

interface PosManagerCompanion {
    val id: String
    val deviceType: Int
    val module: Module
    fun isCompatible(context: Context): Boolean
    fun setup(context: Context)
}