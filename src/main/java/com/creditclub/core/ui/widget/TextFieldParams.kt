package com.creditclub.core.ui.widget


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 28/08/2019.
 * Appzone Ltd
 */
data class TextFieldParams(
    val hint: String,
    val maxLength: Int = 100,
    val type: String? = null,
    val helperText: String? = null,
    val minLength: Int? = null,
    val initialValue: String? = null
)