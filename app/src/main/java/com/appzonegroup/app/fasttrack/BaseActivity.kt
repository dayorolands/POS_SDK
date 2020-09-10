package com.appzonegroup.app.fasttrack

import android.annotation.SuppressLint
import android.content.Intent
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.appzonegroup.app.fasttrack.model.AppConstants
import com.appzonegroup.app.fasttrack.model.TokenRequest
import com.appzonegroup.app.fasttrack.ui.Dialogs
import com.appzonegroup.app.fasttrack.utility.LocalStorage
import com.appzonegroup.app.fasttrack.utility.task.AsyncResponse
import com.appzonegroup.app.fasttrack.utility.task.PostCallTask
import com.appzonegroup.creditclub.pos.extension.posConfig
import com.creditclub.core.ui.CreditClubActivity
import com.creditclub.core.ui.widget.DialogListenerBlock
import com.creditclub.pos.printer.PosPrinter
import com.google.gson.Gson
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

/**
 * Created by Joseph on 1/21/2018.
 */

@SuppressLint("Registered")
open class BaseActivity : CreditClubActivity(), AsyncResponse {

    val printer: PosPrinter by inject { parametersOf(this, dialogProvider) }
    override val hasLogoutTimer get() = true

    open fun showNotification(message: String) {
        //Toast.makeText(getBaseContext(), message, Toast.LENGTH_LONG).show();
        dialogProvider.hideProgressBar()
        Dialogs.getInformationDialog(this, message, false).show()
    }

    fun showNotification(message: String, shouldClose: Boolean) {
        dialogProvider.hideProgressBar()
        Dialogs.getInformationDialog(this, message, shouldClose).show()
    }

    internal fun showToast(toastMessage: String) {
        Toast.makeText(
            applicationContext, toastMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    fun startActivity(classToStart: Class<*>) {
        startActivity(Intent(this, classToStart))
    }

    @JvmOverloads
    fun sendJSONPostRequestWithCallback(
        url: String,
        data: String,
        sucessCallback: Response.Listener<JSONObject>?,
        errorCallback: Response.ErrorListener?,
        view: View? = null
    ) {
        var sucessCallback = sucessCallback
        var errorCallback = errorCallback
        val queue = Volley.newRequestQueue(this)
        dialogProvider.showProgressBar("loading...")
        var convertedObject: JSONObject? = null
        if (sucessCallback == null) {
            sucessCallback = defaultResponseListener(view)
        }
        if (errorCallback == null) {
            errorCallback = defaultErrorCallback(view)
        }
        if (view != null) {
            view.isEnabled = false
            view.isClickable = false
        }
        try {
            convertedObject = JSONObject(data)
        } catch (e: Exception) {
            Log.e("creditclub", "failed json parsing")
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            convertedObject,
            sucessCallback,
            errorCallback
        )
        queue.add(request)
    }

    fun openPage(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
    }

    fun renderSuccess(s: String?) {
        setContentView(R.layout.layout_success)
        findViewById<TextView>(R.id.success_message_tv).text = s
        findViewById<View>(R.id.success_close_button)
            .setOnClickListener { finish() }
    }

    fun addValidPhoneNumberListener(editText: EditText) {

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                phoneNumberEditTextFilter(editText, this)
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    fun phoneNumberEditTextFilter(editText: EditText, textWatcher: TextWatcher) {
        val numbers = "0123456789"

        val text = editText.text.toString().trim { it <= ' ' }

        val textToCharArray = text.toCharArray()

        val accumulator = StringBuilder()

        for (c in textToCharArray) {
            if (numbers.contains(c + "")) {
                accumulator.append(c)
            }
        }
        editText.removeTextChangedListener(textWatcher)

        //This line without the line before and after will cause endless loop
        //of call to the text changed listener
        editText.setText(accumulator)
        editText.addTextChangedListener(textWatcher)
        editText.setSelection(accumulator.length)
    }

    private fun defaultResponseListener(view: View?): Response.Listener<JSONObject> {
        return Response.Listener { `object` ->
            if (view != null) {
                view.isEnabled = true
                view.isClickable = true
            }
            val result = `object`.toString()
            val response =
                Gson().fromJson(result, com.appzonegroup.app.fasttrack.model.Response::class.java)
            if (response.isSuccessful) {
                dialogProvider.showSuccess(response.reponseMessage)
            } else {
                dialogProvider.showError(response.reponseMessage)
            }
        }
    }

    private fun defaultErrorCallback(view: View?): Response.ErrorListener {
        return Response.ErrorListener {
            if (view != null) {
                view.isEnabled = true
                view.isClickable = true
            }
            dialogProvider.showError("A network-related error just occurred. Please try again later")
        }
    }

    fun sendCustomerToken(customerPhoneNumber: String, amount: String, isPinChange: Boolean) {
        //make the phone number EditText uneditable while sending the token
        //phoneNumberET.setEnabled(false);
        val tkRequest = TokenRequest()
        tkRequest.customerAccountNumber = customerPhoneNumber
        tkRequest.agentPhoneNumber = LocalStorage.getPhoneNumber(baseContext)
        tkRequest.agentPin = LocalStorage.getAgentsPin(baseContext)
        tkRequest.institutionCode = LocalStorage.getInstitutionCode(baseContext)
        tkRequest.amount = amount
        tkRequest.isPinChange = isPinChange

        PostCallTask(dialogProvider, this, this)
            .execute(
                AppConstants.getBaseUrl() + "/CreditClubMiddleWareAPI/CreditClubStatic/SendToken",
                Gson().toJson(tkRequest)
            )
    }

    override fun processFinished(output: String?) {

    }

    fun goBack(v: View) {
        onBackPressed()
    }

    fun showOptions(title: String, options: Array<String>, listener: (Int) -> Unit) {
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setTitle(title)
        alertBuilder.setCancelable(true)

        alertBuilder.setItems(options) { _, i ->
            listener(i)
        }

        alertBuilder.show()
    }

//    fun handleTimeout() {
//        logout {
//            putExtra("SESSION_TIMEOUT", true)
//        }
//    }

    fun <T> catchError(block: () -> T): T? {
        try {
            return block()
        } catch (e: Exception) {
            e.printStackTrace()
            showInternalError()
        }

        return null
    }

    private fun confirmAdminPassword(
        password: String,
        closeOnFail: Boolean = false,
        next: (Boolean) -> Unit
    ) {
        val status = password == posConfig.adminPin
        if (!status) {
            if (closeOnFail) return dialogProvider.showError<Nothing>("Incorrect Password") {
                onClose {
                    finish()
                }
            }

            dialogProvider.showError("Incorrect Password")
        }
        next(status)
    }

    fun adminAction(next: () -> Unit) {
        val passwordType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

        com.appzonegroup.creditclub.pos.widget.Dialogs.input(
            this,
            "Administrator password",
            passwordType
        ) {
            onSubmit { password ->
                dismiss()
                confirmAdminPassword(password) { passed ->
                    if (passed) next()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun showError(message: String?) = dialogProvider.showError(message)
    fun <T> showError(message: String?, block: DialogListenerBlock<T>) =
        dialogProvider.showError(message, block)

    fun showSuccess(message: String?) = dialogProvider.showSuccess(message)
    fun <T> showSuccess(message: String?, block: DialogListenerBlock<T>) =
        dialogProvider.showSuccess(message, block)

    open fun indicateError(message: String?, view: EditText?) =
        dialogProvider.indicateError(message, view)

    fun showProgressBar(title: String) = dialogProvider.showProgressBar(title)
    fun showProgressBar(title: String, message: String?) =
        dialogProvider.showProgressBar(title, message)

    fun hideProgressBar() = dialogProvider.hideProgressBar()

    fun requestPIN(title: String, block: DialogListenerBlock<String>) =
        dialogProvider.requestPIN(title, block)
}
