package com.creditclub.core.data

import android.os.Parcel
import kotlinx.parcelize.Parceler
import java.time.Instant

inline fun <T> Parcel.readNullable(reader: () -> T) =
    if (readInt() != 0) reader() else null

inline fun <T> Parcel.writeNullable(value: T?, writer: T.() -> Unit) {
    if (value != null) {
        writeInt(1)
        value.writer()
    } else {
        writeInt(0)
    }
}

object InstantParceler : Parceler<Instant?> {

    override fun create(parcel: Parcel): Instant? =
        parcel.readNullable { Instant.parse(parcel.readString()) }

    override fun Instant?.write(parcel: Parcel, flags: Int) =
        parcel.writeNullable(this) { parcel.writeString(toString()) }
}