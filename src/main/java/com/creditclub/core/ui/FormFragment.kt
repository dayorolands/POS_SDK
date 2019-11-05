package com.creditclub.core.ui

import com.creditclub.core.contract.FormDataHolder


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 05/10/2019.
 * Appzone Ltd
 */
abstract class FormFragment<T> : CreditClubFragment() {
    val formData get() = (activity as FormDataHolder<T>).formData
}