package com.appzonegroup.creditclub.pos.util

import android.content.Intent
import com.appzonegroup.creditclub.pos.PosActivity
import com.appzonegroup.creditclub.pos.R


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 3/1/2019.
 * Appzone Ltd
 */

open class ActionButton {
    open var id: Int = 0
    open var icon: Int = R.mipmap.ic_launcher_round
    open var name: String = ""
    var activityClass: Class<*>? = null
    val isClickable get() = action != null || activityClass != null
    private var action: ((PosActivity) -> Unit)? = null

    fun onClick(listener: (PosActivity) -> Unit) {
        action = listener
    }

//    fun <T : PosCommand> onClick(commandFactory: () -> T) {
//        action = {
//            val instance = commandFactory()
//
//            instance.run()
//        }
//    }

    fun click(activity: PosActivity) {
        action?.invoke(activity)
        activityClass?.also {
            activity.startActivity(Intent(activity, activityClass))
        }
    }
}

fun actionButton(block: ActionButton.() -> Unit) = ActionButton().apply(block)