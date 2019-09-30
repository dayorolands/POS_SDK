package com.appzonegroup.app.fasttrack.ui

import android.app.Activity
import android.app.Dialog
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.appzonegroup.app.fasttrack.R
import com.appzonegroup.creditclub.pos.contract.DialogListener
import com.appzonegroup.creditclub.pos.contract.DialogListenerBlock
import com.appzonegroup.creditclub.pos.contract.DialogProvider
import kotlinx.android.synthetic.main.dialog_error.*
import kotlinx.android.synthetic.main.dialog_progress_layout.*


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/29/2019.
 * Appzone Ltd
 */
class PosDialogProvider(val activity: Activity): DialogProvider {
    private val progressDialog by lazy { Dialogs.getProgress(activity, null) }

    override fun hideProgressBar() {
        activity.runOnUiThread {
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }

    override fun showError(message: String) {
        activity.runOnUiThread {
            hideProgressBar()
            Dialogs.getErrorDialog(activity, message).show()
        }
    }

    override fun showError(message: String, next: DialogListenerBlock<Nothing>) {
        activity.runOnUiThread {
            hideProgressBar()
            val dialog = Dialogs.getErrorDialog(activity, message)
            val dialogConfig = DialogListener<Nothing>().apply(next)
            dialog.close_btn.setOnClickListener {
                dialog.dismiss()
                dialogConfig.close()
            }
            dialog.show()
        }
    }

    override fun showInfo(message: String, next: DialogListenerBlock<Nothing>): Dialog {
        val dialogConfig = DialogListener<Nothing>().apply(next)
        val dialog = Dialogs.getErrorDialog(activity, message)
        activity.   runOnUiThread {
            hideProgressBar()
            dialog.close_btn.setOnClickListener {
                dialog.dismiss()
                dialogConfig.close()
            }
            dialog.show()
        }

        return dialog
    }

    override fun showSuccess(message: String) {
        activity.  runOnUiThread {
            hideProgressBar()
            Dialogs.getSuccessDialog(activity, message).show()
        }
    }

    override fun showSuccess(message: String, next: DialogListener<Nothing>.() -> Unit) {
        activity.  runOnUiThread {
            hideProgressBar()
            val dialog = Dialogs.getSuccessDialog(activity, message)
            val dialogConfig = DialogListener<Nothing>().apply(next)
            dialog.close_btn.setOnClickListener {
                dialog.dismiss()
                dialogConfig.close()
            }
            dialog.show()
        }
    }

    override fun indicateError(message: String, view: EditText?) {
        hideProgressBar()
        view?.also {
            view.isFocusable = true
            view.isEnabled = true
            view.error = message
            //        showNotification(message);
            view.requestFocus()
        }
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }


    private fun handleProgressBar(
            title: String = activity.resources.getString(R.string.loading),
            message: String? = activity.resources.getString(R.string.please_wait),
            isCancellable: Boolean = false,
            config: (DialogListener<Nothing>.() -> Unit)? = null
    ): Dialog {
        activity. runOnUiThread {
            progressDialog.findViewById<TextView>(R.id.message_tv).text = message
            progressDialog.header_tv.text = title

            progressDialog.findViewById<View>(R.id.cancel_button).run {
                visibility = if (isCancellable) View.VISIBLE else View.GONE
                setOnClickListener {
                    if (progressDialog.isShowing) progressDialog.dismiss()
                }
            }

            if (config != null) {
                val dialogConfig = DialogListener<Nothing>().apply(config)
                progressDialog.findViewById<View>(R.id.cancel_button).setOnClickListener {
                    if (progressDialog.isShowing) progressDialog.dismiss()
                    dialogConfig.close()
                }
            } else progressDialog.setOnCancelListener { }

            if (!progressDialog.isShowing) progressDialog.show()
        }

        return progressDialog
    }

    override fun showProgressBar(
            title: String,
            message: String?,
            isCancellable: Boolean,
            config: (DialogListener<Nothing>.() -> Unit)?
    ): Dialog {
        return handleProgressBar(title, message, isCancellable, config)
    }

    override fun showProgressBar(title: String): Dialog {
        return handleProgressBar(title)
    }

    override fun showProgressBar(title: String, message: String?): Dialog {
        return handleProgressBar(title, message)
    }

    override fun showProgressBar(
            title: String,
            message: String?,
            config: (DialogListener<Nothing>.() -> Unit)?
    ): Dialog {
        return handleProgressBar(title, message, config = config)
    }
}