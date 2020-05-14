package com.creditclub.core.data.model

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
class ApiRequestLog {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @SerialName("UniqueID")
    var uniqueId: String = ""

    @SerialName("InstitutionCode")
    var institutionCode: String = ""

    @SerialName("RequestType")
    var requestType: String = ""

    @SerialName("AppName")
    var appName: String = ""

    @SerialName("AppVersionName")
    var appVersionName: String = ""

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
}

@Dao
interface ApiRequestLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAll(requestLog: List<ApiRequestLog>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(apiRequestLog: ApiRequestLog)

    @Update
    fun update(apiRequestLog: ApiRequestLog)

    @Delete
    fun delete(apiRequestLog: ApiRequestLog)

    @Query("DELETE FROM ApiRequestLog")
    fun deleteAll()

    @Query("Delete from ApiRequestLog where id >= :start AND ID <= :end")
    fun deleteRange(start: Int, end: Int)

    @Query("Delete from ApiRequestLog where id = :id")
    fun delete(id: Int)

    @Query("SELECT * FROM ApiRequestLog")
    fun allAsync(): LiveData<List<ApiRequestLog>>

    @Query("SELECT * FROM ApiRequestLog")
    fun all(): List<ApiRequestLog>
}