package com.appzonegroup.creditclub.pos

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.appzonegroup.creditclub.pos.contract.DialogListener
import com.appzonegroup.creditclub.pos.contract.DialogListenerBlock
import com.appzonegroup.creditclub.pos.contract.DialogProvider
import com.appzonegroup.creditclub.pos.widget.Dialogs
import com.creditclub.core.ui.CreditClubActivity
import kotlinx.android.synthetic.main.dialog_progress_layout.*
import kotlinx.android.synthetic.main.pos_dialog_error.*


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 6/26/2019.
 * Appzone Ltd
 */
@SuppressLint("Registered")
abstract class DialogProviderActivity : CreditClubActivity(), DialogProvider {
    private val progressDialog by lazy { Dialogs.getProgress(this, null) }

    override fun hideProgressBar() {
        runOnUiThread {
            if (progressDialog.isShowing) {
                progressDialog.dismiss()
            }
        }
    }

    override fun showError(message: String) {
        runOnUiThread {
            hideProgressBar()
            Dialogs.getErrorDialog(this, message).show()
        }
    }

    override fun showError(message: String, next: DialogListenerBlock<Nothing>) {
        runOnUiThread {
            hideProgressBar()
            val dialog = Dialogs.getErrorDialog(this, message)
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
        val dialog = Dialogs.getErrorDialog(this, message)
        runOnUiThread {
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
        runOnUiThread {
            hideProgressBar()
            Dialogs.getSuccessDialog(this, message).show()
        }
    }

    override fun showSuccess(message: String, next: DialogListenerBlock<Nothing>) {
        runOnUiThread {
            hideProgressBar()
            val dialog = Dialogs.getSuccessDialog(this, message)
            val dialogConfig = DialogListener<Nothing>().apply(next)
            dialog.close_btn.setOnClickListener {
                dialog.dismiss()
                dialogConfig.close()
            }
            dialog.show()
        }
    }

    fun renderSuccess(s: String) {
        setContentView(R.layout.layout_success)
        findViewById<TextView>(R.id.success_message_tv).text = s
        findViewById<View>(R.id.success_close_button).setOnClickListener { finish() }
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
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }


    fun handleProgressBar(
        title: String = resources.getString(R.string.loading),
        message: String? = resources.getString(R.string.please_wait),
        isCancellable: Boolean = false,
        config: (DialogListenerBlock<Nothing>)? = null
    ): Dialog {
        runOnUiThread {
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
        config: DialogListenerBlock<Nothing>?
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
        config: DialogListenerBlock<Nothing>?
    ): Dialog {
        return handleProgressBar(title, message, config = config)
    }

    fun openPage(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
    }
}