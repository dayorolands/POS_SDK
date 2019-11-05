package com.appzonegroup.creditclub.pos.models

import androidx.room.*
import com.appzonegroup.creditclub.pos.models.messaging.BaseIsoMsg

/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 2/25/2019.
 * Appzone Ltd
 */

@Entity
open class Reversal : FinancialTransaction {
    constructor() : super()

    constructor(isoMsg: BaseIsoMsg) : super(isoMsg)
}

@Dao
interface ReversalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAll(reversals: List<Reversal>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(reversal: Reversal)

    @Update
    fun update(reversal: Reversal)

    @Delete
    fun delete(reversal: Reversal)

    @Query("DELETE FROM Reversal WHERE date != :date")
    fun deleteOthers(date: String)

    @Query("DELETE FROM Reversal")
    fun deleteAll()

    @Query("SELECT * FROM Reversal")
    fun all(): List<Reversal>

    @Query("SELECT * FROM Reversal ORDER BY id DESC LIMIT 1")
    fun lastTransaction(): Reversal?

    @Query("SELECT * FROM Reversal WHERE stan = :stan")
    fun byStan(stan: String): Reversal?

    @Query("SELECT * FROM Reversal WHERE date = :date LIMIT 200")
    fun byDate(date: String): List<Reversal>
}