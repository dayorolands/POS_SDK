package com.dspread.qpos.utils

import com.creditclub.pos.extensions.bytesToHexString
import com.creditclub.pos.extensions.stringToHexBytes

internal inline val ByteArray.hexString: String get() = bytesToHexString(this)

internal inline val String.hexBytes: ByteArray get() = stringToHexBytes(this)