package com.appzonegroup.creditclub.pos.models

import androidx.lifecycle.LiveData
import androidx.room.*
import com.creditclub.core.serializer.TimeInstantSerializer
import com.creditclub.pos.model.ConnectionInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant


/**
 * Created by Emmanuel Nosakhare <enosakhare@appzonegroup.com> on 5/14/2019.
 * Appzone Ltd
 */

@Entity
@Serializable
class IsoRequestLog {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @SerialName("UniqueID")
    var uniqueId: String = ""

    @SerialName("InstitutionCode")
    var institutionCode: String = ""

    @SerialName("TerminalID")
    var terminalId: String = ""

    @SerialName("RRN")
    var rrn: String = ""

    @SerialName("TransactionType")
    var transactionType: String = ""

    @SerialName("Amount")
    var amount: String = ""

    @SerialName("AgentCode")
    var agentCode: String = ""

    @SerialName("GPSCoordinates")
    var gpsCoordinates: String = ""

    @SerialName("ResponseCode")
    var responseCode: String = ""

    @Serializable(with = TimeInstantSerializer::class)
    @SerialName("RequestTime")
    var requestTime: Instant = Instant.now()

    @Serializable(with = TimeInstantSerializer::class)
    @SerialName("ResponseTime")
    var responseTime: Instant? = null

    @SerialName("NodeName")
    var nodeName: String? = null

    @kotlinx.serialization.Transient
    var connectionInfo: ConnectionInfo? = null
}

@Dao
interface IsoRequestLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAll(requestLog: List<IsoRequestLog>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(isoRequestLog: IsoRequestLog)

    @Update
    fun update(isoRequestLog: IsoRequestLog)

    @Delete
    fun delete(isoRequestLog: IsoRequestLog)

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