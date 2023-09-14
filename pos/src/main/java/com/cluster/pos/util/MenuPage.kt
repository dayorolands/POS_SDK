package com.cluster.pos.util

import android.content.Intent
import com.cluster.pos.PosActivity
import com.cluster.pos.R
import kotlinx.coroutines.launch

/**
 * Created by Ifedayo Adekoya <ifedayo.adekoya@starkitchensgroup.com> on 14/09/2023.
 * Orda Africa
 */

open class ActionButton(
    open val id: Int = 0,
    open val icon: Int = R.drawable.orda_logo_dark_payment,
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
    icon: Int = R.drawable.orda_logo_dark_payment,
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
    icon: Int = R.drawable.orda_logo_dark_payment,
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
    override var icon: Int = R.drawable.orda_logo_dark_payment,
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
    icon: Int = R.drawable.orda_logo_dark_payment,
    name: String = "",
    options: Lazy<List<ActionButton>>? = null,
) = MenuPage(
    isSecure = isSecure,
    id = id,
    icon = icon,
    name = name,
    options = options,
)