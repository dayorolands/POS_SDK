package com.creditclub.core.serializer

import com.creditclub.core.util.toInstant
import io.objectbox.converter.PropertyConverter
import org.threeten.bp.Instant

class TimeInstantConverter : PropertyConverter<Instant, String> {

    override fun convertToDatabaseValue(entityProperty: Instant): String {
        return entityProperty.toString()
    }

    override fun convertToEntityProperty(databaseValue: String): Instant {
        return databaseValue.toInstant()
    }
}