package com.cluster.ui

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import com.cluster.R
import com.google.android.material.textfield.TextInputEditText

/**
 * Created by Joseph on 1/14/2017.
 */
class EditText : TextInputEditText {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setCustomFont(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        setCustomFont(context, attrs)
    }

    private fun setCustomFont(ctx: Context, attrs: AttributeSet) {
        val a = ctx.obtainStyledAttributes(attrs, R.styleable.CustomTextView)
        a.getString(R.styleable.CustomTextView_my_font)

        if (a.getString(R.styleable.CustomTextView_fontColor) != null) {
            setTextColor(context.resources.getColor(R.color.black))
        }

        val customFontSize = a.getString(R.styleable.CustomTextView_fontSize)
        if (customFontSize != null) {
            textSize = java.lang.Float.parseFloat(customFontSize.substring(0, 2))
        }

        a.recycle()
    }

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

    companion object {
        @JvmStatic
        fun deriveInputType(inputType: String? = "text"): Int = when (inputType) {
            "date" -> InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_DATE
            "datetime" -> InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_NORMAL
            "none" -> InputType.TYPE_NULL
            "number" -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
            "numberDecimal" -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            "numberPassword" -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            "numberSigned" -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
            "phone" -> InputType.TYPE_CLASS_PHONE
            "text" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            "textAutoComplete" -> InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE
            "textAutoCorrect" -> InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
            "textCapCharacters" -> InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            "textCapSentences" -> InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            "textCapWords" -> InputType.TYPE_TEXT_FLAG_CAP_WORDS
            "textEmailAddress" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            "textEmailSubject" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_SUBJECT
            "textFilter" -> InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE
            "textLongMessage" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE
            "textMultiLine" -> InputType.TYPE_TEXT_FLAG_MULTI_LINE
            "textNoSuggestions" -> InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            "textPassword" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            "textPersonName" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            "textPhonetic" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PHONETIC
            "textPostalAddress" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS
            "textShortMessage" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE
            "textUri" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            "textVisiblePassword" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            "textWebEditText" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT
            "textWebEmailAddress" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
            "textWebPassword" -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
            "time" -> InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_TIME
            else -> InputType.TYPE_CLASS_TEXT
        }
    }
}
