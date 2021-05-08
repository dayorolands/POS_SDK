package com.appzonegroup.app.fasttrack.model.online

import org.json.JSONObject
import kotlin.Throws
import org.json.JSONArray
import java.lang.Exception
import java.util.*

class Option(item: JSONObject) {
    var name: String? = item.optString("content")
        private set
    var index: String? = item.optString("Index")
        private set

    companion object {
        @JvmStatic
        @Throws(Exception::class)
        fun parseMenu(data: JSONArray): ArrayList<Option> {
            val menuItems = ArrayList<Option>()
            for (i in 0 until data.length()) {
                val menuItem = data.getJSONObject(i)
                menuItems.add(Option(menuItem))
            }
            return menuItems
        }
    }
}