package com.creditclub.core.serializer

import com.creditclub.core.util.CREDIT_CLUB_DATE_PATTERN
import com.creditclub.core.util.format
import com.creditclub.core.util.instantFromPattern
import io.objectbox.converter.PropertyConverter
import java.time.Instant

class TimeInstantConverter : PropertyConverter<Instant, String> {

    override fun convertToDatabaseValue(entityProperty: Instant): String {
        return entityProperty.format(CREDIT_CLUB_DATE_PATTERN)
    }

    override fun convertToEntityProperty(databaseValue: String): Instant {
        return databaseValue.instantFromPattern(CREDIT_CLUB_DATE_PATTERN)
    }
}