package com.appzonegroup.creditclub.pos.util

import com.appzonegroup.creditclub.pos.R

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 3/1/2019.
 * Appzone Ltd
 */

open class MenuPage(
    var options: Lazy<LinkedHashMap<Int, ActionButton>>? = null,
    val isSecure: Boolean = false,
    override var id: Int = 0,
    override var icon: Int = R.drawable.ic_logo_round,
    override var name: String = "",
    override var activityClass: Class<*>? = null,
) : ActionButton(id = id, icon = icon, name = name, activityClass = activityClass) {

    companion object {
        const val PAGE_NUMBER = "PAGE_NUMBER"
        const val IS_SECURE_PAGE = "IS_SECURE_PAGE"
        const val TITLE = "TITLE"
    }
}

inline fun menuPage(isSecure: Boolean = false, crossinline block: MenuPage.() -> Unit) =
    MenuPage(isSecure = isSecure).apply(block)