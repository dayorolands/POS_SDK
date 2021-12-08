package com.cluster.core.data.model

import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.room.*
import com.cluster.core.data.prefs.LocalStorage
import com.cluster.core.util.RAMInfo
import com.cluster.core.util.localStorage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.context.GlobalContext
import java.text.SimpleDateFormat
import java.util.*

@Serializable
@Entity(indices = [Index("sessionID")])
class DeviceTransactionInformation {

    @SerialName("ID")
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    @SerialName("DateReceivedString")
    var dateReceived: String? = null

    @SerialName("DateEndedString")
    var dateEnded: String? = null

    @SerialName("SessionID")
    var sessionID: String? = null

    @SerialName("InstitutionCode")
    var institutionCode: String? = null

    @SerialName("AgentPhoneNumber")
    var agentPhoneNumber: String? = null

    @SerialName("RequestCount")
    var requestCount: Int = 0
        set(requestCount) {
            field = requestCount
            this.setDateEnded()
        }

    @SerialName("SuccessCount")
    var successCount: Int = 0
        set(successCount) {
            field = successCount
            this.setDateEnded()
        }

    @SerialName("NoInternet")
    var noInternet: Int = 0
        set(noInternet) {
            field = noInternet
            this.setDateEnded()
        }

    @SerialName("NoResponse")
    var noResponse: Int = 0
        set(noResponse) {
            field = noResponse
            this.setDateEnded()
        }

    @SerialName("ErrorResponse")
    var errorResponse: Int = 0
        set(errorResponse) {
            field = errorResponse
            this.setDateEnded()
        }

    @SerialName("RamSize")
    var ramSize: String? = null

    @SerialName("PercentageLeftOver")
    var percentageLeftOver: Float = 0.toFloat()

    @SerialName("MemorySpace")
    var memorySpace: String? = null

    @SerialName("MemorySpaceLeft")
    var memorySpaceLeft: String? = null

    @SerialName("AppName")
    var appName: String? = null

    private fun setDateEnded() {
        this.dateEnded = currentDateLongString
    }

    companion object {
        fun getInstance(context: Context): DeviceTransactionInformation {
            return getInstance(context, context.localStorage.sessionID)
        }

        fun getInstance(context: Context, sessionID: String?): DeviceTransactionInformation {
            val information = DeviceTransactionInformation()
            information.agentPhoneNumber = context.localStorage.agentPhone
            information.institutionCode = context.localStorage.institutionCode

            // Get Current sessionID
            information.sessionID = sessionID
            information.dateReceived = currentDateLongString
            information.memorySpace = totalMemory
            information.memorySpaceLeft = availableMemory

            val ramInfo = context.RAMInfo
            information.ramSize = formatMemorySize(ramInfo[0])
            information.percentageLeftOver = ramInfo[0].toFloat() / ramInfo[1].toFloat() * 100.toFloat()

            information.noInternet = getTransactionMonitorCounter(LocalStorage.NoInternetCount)
            information.errorResponse =
                getTransactionMonitorCounter(LocalStorage.ErrorResponseCount)
            information.noResponse = getTransactionMonitorCounter(LocalStorage.NoResponseCount)
            information.requestCount = getTransactionMonitorCounter(LocalStorage.RequestCount)
            information.successCount = getTransactionMonitorCounter(LocalStorage.SuccessCount)

            return information
        }
    }
}

private fun getTransactionMonitorCounter(key: String): Int {
    val localStorage = GlobalContext.get().get<LocalStorage>()
    val value = localStorage.getString(key)
    var count = 0
    if (value != null) count = Integer.parseInt(value)

    localStorage.putString(key, count.toString())
    return count
}

@Dao
interface DeviceTransactionInformationDAO {
    @Query("SELECT * FROM DeviceTransactionInformation WHERE sessionID = :sessionID")
    fun findBySessionID(sessionID: String?): List<DeviceTransactionInformation>

    @Query("SELECT * FROM DeviceTransactionInformation WHERE id = :id")
    fun find(id: String?): DeviceTransactionInformation

    @Query("SELECT * FROM DeviceTransactionInformation")
    fun findAll(): List<DeviceTransactionInformation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveAll(info: List<DeviceTransactionInformation>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(info: DeviceTransactionInformation)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun update(info: DeviceTransactionInformation)

    @Delete
    fun delete(info: DeviceTransactionInformation)

    @Delete
    fun delete(info: List<DeviceTransactionInformation>)

    @Query("DELETE FROM DeviceTransactionInformation")
    fun deleteAll()

    @Query("DELETE FROM DeviceTransactionInformation WHERE ID >= :startID AND ID <= :endID")
    fun deleteRange(startID: Int, endID: Int)
}

inline val currentDateLongString: String
    get() {
        val currentDateTime = Calendar.getInstance().time
        val formatter = SimpleDateFormat("dd-MM-yyyy hh:mm:ss:SSS")
        var part = formatter.format(currentDateTime)
        part += "0000 "
        return part + SimpleDateFormat("a").format(currentDateTime)
    }

inline val totalMemory: String
    get() = formatMemorySize(totalInternalMemorySize + totalExternalMemorySize)

inline val availableMemory: String
    get() = formatMemorySize(availableInternalMemorySize + availableExternalMemorySize)

inline val availableInternalMemorySize: Long
    get() {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize: Long = stat.blockSizeLong

        val availableBlocks: Long = stat.availableBlocksLong

        return availableBlocks * blockSize
    }

inline val totalInternalMemorySize: Long
    get() {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize: Long = stat.blockSizeLong
        val totalBlocks: Long = stat.blockCountLong
        return totalBlocks * blockSize
    }

inline val availableExternalMemorySize: Long
    get() {
        return if (externalMemoryAvailable) {
            val path = Environment. getExternalStorageDirectory()
            val stat = StatFs(path.path)
            val blockSize: Long = stat.blockSizeLong
            val availableBlocks: Long = stat.availableBlocksLong

            availableBlocks * blockSize
        } else {
            -1
        }
    }

inline val totalExternalMemorySize: Long
    get() {
        return if (externalMemoryAvailable) {
            val path = Environment.getExternalStorageDirectory()
            val stat = StatFs(path.path)

            val blockSize: Long = stat.blockSizeLong

            val totalBlocks: Long = stat.blockCountLong

            totalBlocks * blockSize
        } else {
            0
        }
    }

inline val externalMemoryAvailable: Boolean
    get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

fun formatMemorySize(size: Long): String {
    var size = size
    var suffix: String? = null

    if (size >= 1024) {
        suffix = "KB"
        size /= 1024
        if (size >= 1024) {
            suffix = "MB"
            size /= 1024

            if (size >= 1024) {
                suffix = "GB"
                size /= 1024
            }
        }
    }

    val resultBuffer = StringBuilder(size.toString())

    var commaOffset = resultBuffer.length - 3
    while (commaOffset > 0) {
        resultBuffer.insert(commaOffset, ',')
        commaOffset -= 3
    }

    if (suffix != null)
        resultBuffer.append(suffix)
    return resultBuffer.toString()
}