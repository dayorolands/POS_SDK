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

@Database(entities = [FinancialTransaction::class, Reversal::class, PosNotification::class], version = 7)
@TypeConverters(RoomConverters::class)
abstract class PosDatabase : RoomDatabase() {

    abstract fun financialTransactionDao(): FinancialTransactionDao
    abstract fun reversalDao(): ReversalDao
    abstract fun posNotificationDao(): PosNotificationDao

    companion object {

        private var INSTANCE: PosDatabase? = null

        private val sLock = Any()

        fun getInstance(context: Context): PosDatabase {
            synchronized(sLock) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        PosDatabase::class.java, "credit_club_pos.db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }

                return INSTANCE as PosDatabase
            }
        }

        fun <T> open(
            context: Context,
            dispatcher: CoroutineContext = Dispatchers.Main,
            block: suspend CoroutineScope.(PosDatabase) -> T
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