package com.creditclub.core.serializer

import com.creditclub.core.util.toInstant
import com.creditclub.core.util.toOffsetDateTime
import io.objectbox.converter.PropertyConverter
import java.time.Instant

class TimeInstantConverter : PropertyConverter<Instant, String> {

    override fun convertToDatabaseValue(entityProperty: Instant): String {
        return entityProperty.toOffsetDateTime()
    }

    override fun convertToEntityProperty(databaseValue: String): Instant {
        return databaseValue.toInstant()
    }
}