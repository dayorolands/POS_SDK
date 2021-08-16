package com.creditclub.core.serializer

import com.creditclub.core.type.TransactionType
import io.objectbox.converter.PropertyConverter

class TransactionTypeConverter : PropertyConverter<TransactionType?, Int?> {
    override fun convertToEntityProperty(databaseValue: Int?): TransactionType? {
        if (databaseValue == null) {
            return null
        }
        for (transactionType in TransactionType.values()) {
            if (transactionType.code == databaseValue) {
                return transactionType
            }
        }
        throw IllegalArgumentException("transaction type $databaseValue is not supported")
    }

    override fun convertToDatabaseValue(entityProperty: TransactionType?): Int? {
        return entityProperty?.code
    }
}