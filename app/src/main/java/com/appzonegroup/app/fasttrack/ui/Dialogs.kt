package com.appzonegroup.app.fasttrack.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.app.fasttrack.databinding.DialogConfirmBinding
import com.creditclub.core.ui.widget.DialogListener
import com.creditclub.core.ui.widget.DialogListenerBlock

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 3/1/2019.
 * Appzone Ltd
 */

object Dialogs {

    fun getProgress(activity: Context, header: CharSequence?): Dialog {
        val dialog = getDialog(R.layout.dialog_progress_layout, activity)
        header?.also { dialog.findViewById<TextView>(R.id.header_tv).text = header }

        return dialog
    }

    fun getDialog(context: Context): Dialog {
        val dialog = Dialog(context)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        return dialog
    }

    fun getDialog(layoutID: Int, context: Context): Dialog {
        val dialog = getDialog(context)
        dialog.setContentView(layoutID)
        return dialog
    }

    fun getInformationDialog(activity: Activity, message: CharSequence?, shouldClose: Boolean): Dialog {
        val dialog = getDialog(R.layout.dialog_info_success, activity)
        if (message != null)
            (dialog.findViewById(R.id.message_tv) as TextView).text = message

        dialog.findViewById<View>(R.id.ok_btn).setOnClickListener {
            dialog.dismiss()
            if (shouldClose)
                activity.finish()
        }

        return dialog
    }

    interface PinChangeHandler {
        fun onSelectNumber(view: View) {
            onChange(
                when (view.id) {
                    R.id.number1 -> 1
                    R.id.number2 -> 2
                    R.id.number3 -> 3
                    R.id.number4 -> 4
                    R.id.number5 -> 5
                    R.id.number6 -> 6
                    R.id.number7 -> 7
                    R.id.number8 -> 8
                    R.id.number9 -> 9
                    else -> 0
                }
            )
        }

        fun onBackspacePressed(view: View)

        fun onEnterPressed(view: View)

        fun onChange(digit: Byte)
    }
}