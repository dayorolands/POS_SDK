package com.creditclub.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.creditclub.core.data.model.AppFunctionUsage
import com.creditclub.core.data.model.AppFunctionUsageDao
import com.creditclub.core.data.model.DeviceTransactionInformation
import com.creditclub.core.data.model.DeviceTransactionInformationDAO
import com.creditclub.core.util.CoreDBConverters

@Database(
    entities = [DeviceTransactionInformation::class, AppFunctionUsage::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(CoreDBConverters::class)
abstract class CoreDatabase : RoomDatabase() {
    abstract fun deviceTransactionInformationDao(): DeviceTransactionInformationDAO
    abstract fun appFunctionUsageDao(): AppFunctionUsageDao

    companion object {

        @Volatile
        private var INSTANCE: CoreDatabase? = null

        fun getInstance(context: Context): CoreDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    CoreDatabase::class.java, "credit_club_core.db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}