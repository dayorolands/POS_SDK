package com.appzonegroup.creditclub.pos.util

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 3/1/2019.
 * Appzone Ltd
 */

open class MenuPage : ActionButton() {
    var options: Lazy<LinkedHashMap<Int, ActionButton>>? = null
    var isSecure: Boolean = false

//    fun options(vararg pairs: Pair<Int, ActionButton>) {
//        this.options = lazy { HashMap<Int, ActionButton>().apply { putAll(pairs) } }
//    }

    companion object {
        const val PAGE_NUMBER = "PAGE_NUMBER"
        const val IS_SECURE_PAGE = "IS_SECURE_PAGE"
        const val TITLE = "TITLE"
    }
}

fun menuPage(block: MenuPage.() -> Unit) = MenuPage().apply(block)