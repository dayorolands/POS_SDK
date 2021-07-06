package com.appzonegroup.creditclub.pos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.appzonegroup.creditclub.pos.models.*
import com.appzonegroup.creditclub.pos.util.RoomConverters
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@Database(
    entities = [
        FinancialTransaction::class,
        Reversal::class,
        PosNotification::class,
        IsoRequestLog::class,
        PosTransaction::class
    ],
    version = 12,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class PosDatabase : RoomDatabase() {

    abstract fun financialTransactionDao(): FinancialTransactionDao
    abstract fun reversalDao(): ReversalDao
    abstract fun posNotificationDao(): PosNotificationDao
    abstract fun isoRequestLogDao(): IsoRequestLogDao
    abstract fun posTransactionDao(): PosTransactionDao

    companion object {

        private var INSTANCE: PosDatabase? = null

        private val sLock = Any()

        fun getInstance(context: Context): PosDatabase {
            synchronized(sLock) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        PosDatabase::class.java, "credit_club_pos.db"
                    ).addMigrations(
                        MIGRATION_7_8,
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                    ).build()
                }

                return INSTANCE as PosDatabase
            }
        }

        inline fun <T> open(
            context: Context,
            dispatcher: CoroutineContext = Dispatchers.Main,
            crossinline block: suspend CoroutineScope.(PosDatabase) -> T
        ) {
            GlobalScope.launch(dispatcher) {
                val inst = withContext(Dispatchers.Default) {
                    getInstance(context)
                }

                block(inst)
            }
        }
    }
}