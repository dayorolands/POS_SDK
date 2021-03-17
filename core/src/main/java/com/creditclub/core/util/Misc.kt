package com.creditclub.core.util

import android.os.Environment
import android.os.StatFs
import java.text.SimpleDateFormat
import java.util.*

object Misc {

    inline val currentDateLongString: String
        get() {
            val currentDateTime = Calendar.getInstance().time
            val formatter = SimpleDateFormat("dd-MM-yyyy hh:mm:ss:SSS")
            var part = formatter.format(currentDateTime)
            part += "0000 "
            return part + SimpleDateFormat("a").format(currentDateTime)
        }

    inline val totalMemory: String
        get() = formatMemorySize(totalInternalMemorySize + totalExternalMemorySize)

    inline val availableMemory: String
        get() = formatMemorySize(availableInternalMemorySize + availableExternalMemorySize)

    inline val availableInternalMemorySize: Long
        get() {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize: Long = stat.blockSizeLong

            val availableBlocks: Long = stat.availableBlocksLong

            return availableBlocks * blockSize
        }

    inline val totalInternalMemorySize: Long
        get() {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize: Long = stat.blockSizeLong
            val totalBlocks: Long = stat.blockCountLong
            return totalBlocks * blockSize
        }

    inline val availableExternalMemorySize: Long
        get() {
            return if (externalMemoryAvailable) {
                val path = Environment.getExternalStorageDirectory()
                val stat = StatFs(path.path)
                val blockSize: Long = stat.blockSizeLong
                val availableBlocks: Long = stat.availableBlocksLong

                availableBlocks * blockSize
            } else {
                -1
            }
        }

    inline val totalExternalMemorySize: Long
        get() {
            return if (externalMemoryAvailable) {
                val path = Environment.getExternalStorageDirectory()
                val stat = StatFs(path.path)

                val blockSize: Long = stat.blockSizeLong

                val totalBlocks: Long = stat.blockCountLong

                totalBlocks * blockSize
            } else {
                0
            }
        }

    inline val externalMemoryAvailable: Boolean
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    fun formatMemorySize(size: Long): String {
        var size = size
        var suffix: String? = null

        if (size >= 1024) {
            suffix = "KB"
            size /= 1024
            if (size >= 1024) {
                suffix = "MB"
                size /= 1024

                if (size >= 1024) {
                    suffix = "GB"
                    size /= 1024
                }
            }
        }

        val resultBuffer = StringBuilder(size.toString())

        var commaOffset = resultBuffer.length - 3
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',')
            commaOffset -= 3
        }

        if (suffix != null)
            resultBuffer.append(suffix)
        return resultBuffer.toString()
    }
}
