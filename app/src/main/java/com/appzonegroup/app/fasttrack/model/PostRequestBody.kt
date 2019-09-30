package com.appzonegroup.app.fasttrack.model

import com.google.gson.Gson

open class PostRequestBody {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
