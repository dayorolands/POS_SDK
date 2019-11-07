package com.appzonegroup.creditclub.pos.models

import androidx.lifecycle.LiveData
import androidx.room.*
import com.creditclub.core.data.contract.IISoRequestLog
import com.creditclub.core.serializer.TimeInstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.threeten.bp.Instant


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/14/2019.
 * Appzone Ltd
 */

@Entity
@Serializable
class IsoRequestLog : IISoRequestLog {

    @PrimaryKey(autoGenerate = true)
    override var id: Long = 0

    @SerialName("UniqueID")
    override var uniqueId: String = ""

    @SerialName("InstitutionCode")
    override var institutionCode: String = ""

    @SerialName("TerminalID")
    override var terminalId: String = ""

    @SerialName("RRN")
    override var rrn: String = ""

    @SerialName("TransactionType")
    override var transactionType: String = ""

    @SerialName("Amount")
    override var amount: String = ""

    @SerialName("AgentCode")
    override var agentCode: String = ""

    @SerialName("GPSCoordinates")
    override var gpsCoordinates: String = ""

    @SerialName("ResponseCode")
    override var responseCode: String = ""

    @Serializable(with = TimeInstantSerializer::class)
    @SerialName("RequestTime")
    override var requestTime: Instant = Instant.now()

    @Serializable(with = TimeInstantSerializer::class)
    @SerialName("ResponseTime")
    override var responseTime: Instant = requestTime
}

@Dao
interface IsoRequestLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAll(requestLog: List<IsoRequestLog>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(posNotification: IsoRequestLog)

    @Update
    fun update(posNotification: IsoRequestLog)

    @Delete
    fun delete(posNotification: IsoRequestLog)

    @Query("DELETE FROM IsoRequestLog")
    fun deleteAll()

    @Query("Delete from IsoRequestLog where id >= :start AND ID <= :end")
    fun deleteRange(start: Int, end: Int)

    @Query("Delete from IsoRequestLog where id = :id")
    fun delete(id: Int)

    @Query("SELECT * FROM IsoRequestLog")
    fun allAsync(): LiveData<List<IsoRequestLog>>

    @Query("SELECT * FROM IsoRequestLog")
    fun all(): List<IsoRequestLog>
}