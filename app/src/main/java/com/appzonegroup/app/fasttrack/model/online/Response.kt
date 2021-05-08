package com.appzonegroup.app.fasttrack.model.online

/**
 * Created by fdamilola on 9/5/15.
 */
object Response {
    @JvmStatic
    fun fixResponse(result: String): String {
        val indexOf = result.indexOf("<!DOCTYPE")
        return if (indexOf > 0) {
            result.substring(0, indexOf)
        } else result
    }
}