package com.creditclub.core.ui.widget


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 28/08/2019.
 * Appzone Ltd
 */
data class DialogConfirmParams(
    val title: String,
    val subtitle: String = "",
    val yesButtonTex: String = "Ok",
    val noButtonTex: String = "Cancel"
)