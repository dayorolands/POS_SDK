package com.creditclub.core.ui.widget

import java.time.LocalDate


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 23/09/2019.
 * Appzone Ltd
 */
data class DateInputParams(val title: String, val maxDate: LocalDate? = null, val minDate: LocalDate? = null)

