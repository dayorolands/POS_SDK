package com.cluster.core.util

import android.content.Context

object SharedPref {
    @Synchronized
    operator fun set(context: Context, key: String?, value: String?) {
        val sp = context.getSharedPreferences("KEY", Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString(key, value)
        editor.commit()
    }

    @Synchronized
    operator fun get(context: Context, key: String?, defVal: String?): String? {
        val sp = context.getSharedPreferences("KEY", Context.MODE_PRIVATE)
        return sp.getString(key, defVal)
    }
}

object XORorAndorOR {
    @Synchronized
    fun XORorANDorORfunction(valueA: String, valueB: String, symbol: String = "|"): String {
        val a = valueA.toCharArray()
        val b = valueB.toCharArray()
        var result = ""

        for (i in 0 until a.lastIndex + 1) {
            result += if (symbol === "|") {
                (Integer.parseInt(a[i].toString(), 16).or
                    (Integer.parseInt(b[i].toString(), 16)).toString(16).toUpperCase())
            } else if (symbol === "^") {
                (Integer.parseInt(a[i].toString(), 16).xor
                    (Integer.parseInt(b[i].toString(), 16)).toString(16).toUpperCase())
            } else {
                (Integer.parseInt(a[i].toString(), 16).and
                    (Integer.parseInt(b[i].toString(), 16))).toString(16).toUpperCase()
            }
        }
        return result
    }
}