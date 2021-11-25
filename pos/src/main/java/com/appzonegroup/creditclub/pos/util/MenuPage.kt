package com.appzonegroup.creditclub.pos.util

import android.content.Intent
import com.appzonegroup.creditclub.pos.PosActivity
import com.appzonegroup.creditclub.pos.R
import kotlinx.coroutines.launch

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 3/1/2019.
 * Appzone Ltd
 */

open class ActionButton(
    open val id: Int = 0,
    open val icon: Int = R.drawable.ic_logo_round,
    open val name: String = "",
    open val activityClass: Class<*>? = null,
    private val action: (suspend (PosActivity) -> Unit)? = null,
) {
    val isClickable get() = action != null || activityClass != null

    fun performClick(activity: PosActivity) {
        activity.mainScope.launch {
            action?.invoke(activity)
        }
        activityClass?.also {
            activity.startActivity(Intent(activity, activityClass))
        }
    }
}

fun actionButton(
    id: Int = 0,
    icon: Int = R.drawable.ic_logo_round,
    name: String = "",
    activityClass: Class<*>? = null,
) = ActionButton(
    id = id,
    icon = icon,
    name = name,
    activityClass = activityClass,
)

fun actionButton(
    id: Int = 0,
    icon: Int = R.drawable.ic_logo_round,
    name: String = "",
    onClick: (suspend (PosActivity) -> Unit)? = null,
) = ActionButton(
    id = id,
    icon = icon,
    name = name,
    action = onClick,
)


class MenuPage(
    val options: Lazy<List<ActionButton>>? = null,
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

fun menuPage(
    isSecure: Boolean = false,
    id: Int = 0,
    icon: Int = R.drawable.ic_logo_round,
    name: String = "",
    options: Lazy<List<ActionButton>>? = null,
) = MenuPage(
    isSecure = isSecure,
    id = id,
    icon = icon,
    name = name,
    options = options,
)