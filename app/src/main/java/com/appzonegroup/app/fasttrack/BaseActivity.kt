package com.appzonegroup.app.fasttrack

import android.annotation.SuppressLint
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.appzonegroup.app.fasttrack.ui.Dialogs
import com.appzonegroup.app.fasttrack.utility.task.AsyncResponse
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

    fun showNotification(message: String, shouldClose: Boolean) {
        dialogProvider.hideProgressBar()
        Dialogs.getInformationDialog(this, message, shouldClose).show()
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
            sucessCallback = Response.Listener { `object` ->
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
        if (errorCallback == null) {
            errorCallback = Response.ErrorListener {
                if (view != null) {
                    view.isEnabled = true
                    view.isClickable = true
                }
                dialogProvider.showError("A network-related error just occurred. Please try again later")
            }
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

    override fun processFinished(output: String?) {

    }

    fun goBack(v: View) {
        onBackPressed()
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
    fun showError(message: String?, block: DialogListenerBlock<*>) =
        dialogProvider.showError(message, block)

    fun showSuccess(message: String?) = dialogProvider.showSuccess(message)
    fun showSuccess(message: String?, block: DialogListenerBlock<*>) =
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
