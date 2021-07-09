package com.creditclub.core.ui.widget

import java.time.LocalDate


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 28/08/2019.
 * Appzone Ltd
 */
data class DialogParams(val title: String, val subtitle: String = "")

data class TextFieldParams(
    val hint: String,
    val maxLength: Int = 100,
    val type: String? = null,
    val helperText: String? = null,
    val minLength: Int = 0,
    val initialValue: String = "",
    val required: Boolean = false,
)

data class DialogConfirmParams(
    val title: CharSequence,
    val subtitle: CharSequence? = null,
    val yesButtonTex: CharSequence = "Ok",
    val noButtonTex: CharSequence = "Cancel"
)

data class DateInputParams(val title: String, val maxDate: LocalDate? = null, val minDate: LocalDate? = null)
data class DialogOptionItem(
    val title: String,
    val subtitle: String? = null,
    val extraInfo: String? = null,
)