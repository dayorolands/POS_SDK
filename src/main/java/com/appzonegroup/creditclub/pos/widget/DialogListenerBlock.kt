package com.appzonegroup.creditclub.pos.widget

import com.appzonegroup.creditclub.pos.models.PosNotification

typealias PosNotificationListenerBlock = PosNotificationListener.() -> Unit
typealias PosNotificationBlock = (PosNotification) -> Unit

class PosNotificationListener {
    private var deleteListener: PosNotificationBlock? = null
    private var settleListener: PosNotificationBlock? = null

    fun onDelete(next: PosNotificationBlock) {
        deleteListener = next
    }

    fun delete(posNotification: PosNotification) {
        deleteListener?.invoke(posNotification)
    }

    fun onSettle(next: PosNotificationBlock) {
        settleListener = next
    }

    fun settle(posNotification: PosNotification) {
        settleListener?.invoke(posNotification)
    }

    companion object {
        inline fun create(block: PosNotificationListenerBlock): PosNotificationListener {
            return PosNotificationListener().apply(block)
        }
    }
}