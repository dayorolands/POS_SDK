package com.cluster.ui.widget

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.textfield.TextInputEditText

class EditText : TextInputEditText {

    init {
        customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onDestroyActionMode(mode: ActionMode?) {}

            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                return false
            }
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    companion object {

        private val TAG = "EditText"
        var inputTypes: MutableMap<String, Int> = HashMap()

        init {
            inputTypes["date"] =
                InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_DATE
            inputTypes["datetime"] =
                InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_NORMAL
            inputTypes["none"] = InputType.TYPE_NULL
            inputTypes["number"] =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
            inputTypes["numberDecimal"] =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            inputTypes["numberPassword"] =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            inputTypes["numberSigned"] =
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
            inputTypes["phone"] = InputType.TYPE_CLASS_PHONE
            inputTypes["text"] = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            inputTypes["textAutoComplete"] = InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
            inputTypes["textAutoCorrect"] = InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
            inputTypes["textCapCharacters"] = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            inputTypes["textCapSentences"] = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            inputTypes["textCapWords"] = InputType.TYPE_TEXT_FLAG_CAP_WORDS
            inputTypes["textEmailAddress"] =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            inputTypes["textEmailSubject"] =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT
            inputTypes["textFilter"] = InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE
            inputTypes["textLongMessage"] =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE
            inputTypes["textMultiLine"] = InputType.TYPE_TEXT_FLAG_MULTI_LINE
            inputTypes["textNoSuggestions"] = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            inputTypes["textPassword"] =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            inputTypes["textPersonName"] =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            inputTypes["textPhonetic"] =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PHONETIC
            inputTypes["textPostalAddress"] =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
            inputTypes["textShortMessage"] =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE
            inputTypes["textUri"] = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            inputTypes["textVisiblePassword"] =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            inputTypes["textWebEditText"] =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT
            inputTypes["textWebEmailAddress"] =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
            inputTypes["textWebPassword"] =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            inputTypes["time"] =
                InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_TIME
        }

        @JvmStatic
        fun deriveInputType(t: String?): Int {
            return try {
                inputTypes[t ?: "text"]!!
            } catch (ignored: Exception) {
                InputType.TYPE_CLASS_TEXT
            }
        }
    }
}
