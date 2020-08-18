package com.appzonegroup.creditclub.pos.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_9_10 = object : Migration(9, 10) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `PosNotification` ADD COLUMN `nodeName` TEXT")
        database.execSQL("ALTER TABLE `PosNotification` ADD COLUMN `connectionInfo` TEXT")

        database.execSQL("ALTER TABLE `IsoRequestLog` ADD COLUMN `nodeName` TEXT")
        database.execSQL("ALTER TABLE `IsoRequestLog` ADD COLUMN `connectionInfo` TEXT")

        database.execSQL("ALTER TABLE `PosTransaction` ADD COLUMN `nodeName` TEXT")
        database.execSQL("ALTER TABLE `PosTransaction` ADD COLUMN `connectionInfo` TEXT")

        database.execSQL("ALTER TABLE `FinancialTransaction` ADD COLUMN `nodeName` TEXT")
        database.execSQL("ALTER TABLE `FinancialTransaction` ADD COLUMN `connectionInfo` TEXT")

        database.execSQL("ALTER TABLE `Reversal` ADD COLUMN `nodeName` TEXT")
        database.execSQL("ALTER TABLE `Reversal` ADD COLUMN `connectionInfo` TEXT")
    }
}