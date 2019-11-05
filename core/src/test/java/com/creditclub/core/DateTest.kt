package com.creditclub.core

import com.creditclub.core.util.timeAgo
import com.creditclub.core.util.toInstant
import org.junit.Test


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 13/09/2019.
 * Appzone Ltd
 */
class DateTest {
    @Test
    fun `date string can be converted to instant`() {
        val dateString = "2019-09-13T17:00:05"
        val instant = dateString.toInstant("yyyy-MM-dd'T'HH:mm:ss[.SSS][xxx][xx][X]")

        val duration = instant.timeAgo()
    }
}