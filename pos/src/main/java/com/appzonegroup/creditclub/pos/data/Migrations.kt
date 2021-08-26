package com.appzonegroup.creditclub.pos.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal val MIGRATION_7_8 = object : Migration(7, 8) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE `IsoRequestLog` (`id` INTEGER, `uniqueId` TEXT,`institutionCode` TEXT," +
                    "`terminalId` TEXT,`rrn` TEXT,`transactionType` TEXT,`amount` TEXT,`agentCode` TEXT," +
                    "`gpsCoordinates` TEXT,`responseCode` TEXT,`requestTime` TEXT,`responseTime` TEXT, " +
                    "PRIMARY KEY(`id`))"
        )
    }
}

internal val MIGRATION_8_9 = object : Migration(8, 9) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `PosTransaction` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`bankName` TEXT, `agentName` TEXT, `agentCode` TEXT, " +
                    "`agentPhoneNumber` TEXT, `institutionCode` TEXT, `pan` TEXT, " +
                    "`terminalId` TEXT, `transactionType` TEXT, `stan` TEXT, `amount` TEXT, " +
                    "`cardType` TEXT, `expiryDate` TEXT, `responseCode` TEXT, " +
                    "`retrievalReferenceNumber` TEXT, `appName` TEXT, `ptsp` TEXT, " +
                    "`website` TEXT, `merchantDetails` TEXT, `merchantId` TEXT, " +
                    "`cardHolder` TEXT, `dateTime` TEXT, `isASystemChange` INTEGER NOT NULL)"
        )
    }
}

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

internal val MIGRATION_10_11 = object : Migration(10, 11) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `PosNotification` ADD COLUMN `terminalId` TEXT")
    }
}

internal val MIGRATION_11_12 = object : Migration(11, 12) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `PosTransaction` ADD COLUMN `isSynced` INTEGER NOT NULL DEFAULT 0")
    }
}

internal val MIGRATION_12_13 = object : Migration(12, 13) {

    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `PosTransaction` ADD COLUMN `duration` LONG NOT NULL DEFAULT 0")
    }
}