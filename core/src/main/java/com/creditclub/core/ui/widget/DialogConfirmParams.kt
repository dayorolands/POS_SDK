package com.creditclub.core.ui.widget


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 28/08/2019.
 * Appzone Ltd
 */
data class DialogConfirmParams(
    val title: CharSequence,
    val subtitle: CharSequence? = null,
    val yesButtonTex: CharSequence = "Ok",
    val noButtonTex: CharSequence = "Cancel"
)