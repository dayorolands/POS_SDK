package com.appzonegroup.creditclub.pos.extension

import com.creditclub.pos.PosConfig
import com.creditclub.pos.PosManager
import com.creditclub.pos.PosParameter
import com.creditclub.pos.printer.PosPrinter
import org.koin.core.KoinComponent
import org.koin.core.get

inline val KoinComponent.posParameter get() = get<PosParameter>()
inline val KoinComponent.posConfig get() = get<PosConfig>()
inline val KoinComponent.posManager get() = get<PosManager>()
inline val KoinComponent.posPrinter get() = get<PosPrinter>()